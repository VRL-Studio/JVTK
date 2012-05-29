/*
 * Copyright 2012 Michael Hoffer <info@michaelhoffer.de>. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY Michael Hoffer <info@michaelhoffer.de> "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL Michael Hoffer <info@michaelhoffer.de> OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and should not be interpreted as representing official policies, either expressed
 * or implied, of Michael Hoffer <info@michaelhoffer.de>.
 */
package eu.mihosoft.vtk;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import sun.awt.image.ByteInterleavedRaster;
import vtk.vtkPanel;
import vtk.vtkRenderWindow;
import vtk.vtkRenderer;
import vtk.vtkUnsignedCharArray;

/**
 * Swing component that displays the content of a {@link vtk.vtkPanel}. In
 * contrast to the original vtk panel this component is a lightweight component.
 * Although this slows down rendering it may be usefull if transparency and
 * layering of components shall be used. This panle gives full access to the
 * offscreen image which allows to postprocess the image with AWT/Swing.
 *
 * <p>In addition to {@link vtk.vtkPanel} this component provides a fullscreen
 * mode that can be enabled either manually through
 * {@link #enterFullscreenMode() } or by double clicking on the component.</p>
 *
 * @author Michael Hoffer <info@michaelhoffer.de>
 */
public class VTKJPanel extends JPanel
        implements MouseListener, MouseMotionListener, KeyListener {

    private static final long serialVersionUID = 1L;
    //
    // vtk objects
    //
    private vtkRenderWindow rw;
    private vtkPanel panel;
    private vtkRenderer ren;
    //
    //fullscreen component
    //
    private JFrame window;
    //
    // offscreen image
    private Image img;
    //
    // offsets for sample model
    private final int[] bOffs = {0, 1, 2, 3};
    //
    // sample model (used by writable raster)
    private SampleModel sampleModel;
    //
    // transform to get around the axis problem
    // @vtk devs why din't you choose the "right" orientation ;)
    private AffineTransform at;
    //
    // render data (contains data for the offscreen image)
    private byte[] renderData;
    //
    // the color model for the offscreen image
    private static ColorModel colorModel = createColorModel();
    //
    // indicates whether rendering content
    private boolean renderContent;
    //
    // indicates whether this panel is in fullscreen mode
    private boolean fullscreen;
    //
    // indicates whether the content has changed and whether this component
    // shall be rerendered
    private boolean contentChanged;
    //
    // alpha value of vtk content. allows transparency.
    private float contentAlpha = 1.f;

    /**
     * Constructor.
     */
    public VTKJPanel() {

        // panel wich leaves fullscreen if ESC is pressed
        panel = new vtkPanel() {

            @Override
            public void keyPressed(KeyEvent e) {

                super.keyPressed(e);

                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    leaveFullscreenMode();
                }
            }
        };

        rw = panel.GetRenderWindow();
        ren = panel.GetRenderer();

        // create the window
        window = new JFrame();
        window.setUndecorated(true);

        // we add the panel to give it access to native memory etc.
        window.getContentPane().add(panel);

        // unfortunately a window has to be visible to be initialized.
        // that is why we toggle visibility
        // this window does not have a title bar this is not visible (hopefully)
        window.setVisible(true);
        window.setVisible(false);

        // we force render
        contentChanged();

        // double click will leave fullscreen mode
        panel.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {

                    leaveFullscreenMode();
                }
            }
        });

        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);
        setBackground(new Color(120, 120, 120));

        setOpaque(true);
    }

//    public void init() {
//        
//
//    }
    /**
     * Leaves fullscreen mode.
     */
    public void leaveFullscreenMode() {
        GraphicsUtil.leaveFullscreenMode(window);
        window.setVisible(false);
        fullscreen = false;

        this.setSize(1, 1);
        this.revalidate();
        this.setSize(getSize());

        repair();
    }

    /**
     * Enters fullscreen mode.
     */
    public void enterFullscreenMode() {
        GraphicsUtil.enterFullscreenMode(window);
        fullscreen = true;
//        this.setSize(getSize());
    }

    /**
     * Reports rendering capabilites.
     *
     * @see vtk.vtkPanel#Report()
     */
    public void report() {
        panel.Report();

    }

    /**
     * Defines the background color of this panel. This method will also set the
     * background color of the vtk renderer. <p> <b>Note:</b> only red, green
     * abd blue values are used. Alpha is ignored. </p>
     *
     * @param c color to set
     */
    @Override
    public void setBackground(Color c) {
        super.setBackground(c);
        if (ren != null) {
            ren.SetBackground(
                    c.getRed() / 255.f,
                    c.getGreen() / 255.f,
                    c.getBlue() / 255.f);
        }
    }

    /**
     * Returns the vtk renderer used by this panel.
     *
     * @return vtk renderer
     */
    public vtkRenderer getRenderer() {
        return ren;
    }

    /**
     * Returns the vtk render window used by this panel.
     *
     * @return
     */
    public vtkRenderWindow getRenderWindow() {
        return rw;
    }

    @Override
    public void setBounds(int x, int y, int w, int h) {
        super.setBounds(x, y, w, h);
        if (window != null && !fullscreen) {
            window.setSize(w, h);
            contentChanged();
        }
    }

    /**
     * Renders this panel.
     */
    private synchronized void render() {
        panel.lock();

        panel.Render();
        renderContent = ren.VisibleActorCount() > 0;
        updateImage();
        contentChanged = false;

        panel.unlock();
    }

    /**
     * Indicates that the content of this component has changed. The next
     * repaint event after calling this method will trigger rendering.
     * <p><b>Note:</b>This method does not directly trigger rendering. Thus,
     * calling it multiple times does not change behavior.</p>
     */
    public void contentChanged() {
        contentChanged = true;
    }

    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        Composite original = g2.getComposite();

        if (getContentAlpha() < 1.f) {
            AlphaComposite ac1 =
                    AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                    getContentAlpha());
            g2.setComposite(ac1);
        }

        g2.drawImage(getImage(), 0, 0, /*
                 * getWidth(), getHeight(),
                 */ null);

        g2.setComposite(original);
    }

    /**
     * Returns the content of this panel as awt image.
     *
     * @return image that contains the rendered content
     */
    private Image getImage() {

        if (img == null || sizeChanged() || contentChanged) {
            render();
        }

        return img;
    }

    /**
     * Indicates whether the render window and the offscreen image differ in
     * size.
     *
     * @return
     * <code>true</code> if sizes differ;
     * <code>false</code> otherwise
     */
    private boolean sizeChanged() {

        int[] renderSize = rw.GetSize();
        int width = renderSize[0];
        int height = renderSize[1];

        return width != img.getWidth(null) - 1
                || height != img.getHeight(null) - 1;
    }

    /**
     * Updates the offscreen image of this panel.
     */
    private synchronized void updateImage() {

        // if we have no content to render nothing to be done
        if (!renderContent) {
            return;
        }

        // size of render window
        int[] renderSize = rw.GetSize();
        int width = renderSize[0];
        int height = renderSize[1];

        // if either samplemodel, the mirror transform are null or
        // render window and offscreen image have different sizes we need to
        // create new sample model and transform
        if (sampleModel == null || at == null || sizeChanged()) {

            // as far as I know vtk uses RGBA component layout (see below)
            sampleModel = new PixelInterleavedSampleModel(
                    DataBuffer.TYPE_BYTE,
                    width + 1, height + 1,
                    4, 4 * (width + 1),
                    bOffs);

            // transform to get around the axis problem
            // @vtk devs why din't you choose the "right" orientation ;)
            at = new AffineTransform(1, 0.0d, 0.0d, -1, 0, height + 1);

            // resize hidden frame if not in fullscreen mode
            if (!fullscreen) {
                window.setSize(getWidth(), getHeight());
            }
        }

        // retrieve the pixeldata from render window
        vtkUnsignedCharArray vtkPixelData = new vtkUnsignedCharArray();
        ren.GetRenderWindow().GetRGBACharPixelData(0, 0, width, height,
                1, vtkPixelData);

        renderData = vtkPixelData.GetJavaArray();
        DataBuffer dbuf = new DataBufferByte(renderData, width * height, 0);

        // we now construct an image raster with sample model (see above)
        WritableRaster raster =
                new ByteInterleavedRaster(sampleModel, dbuf, new Point(0, 0));

        // transform the original raster
        AffineTransformOp op = new AffineTransformOp(
                at, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        WritableRaster mirrorRaster = op.filter(raster, null);

        // finally, create an image
        img = new BufferedImage(colorModel, mirrorRaster, false, null);
    }

    /**
     * Returns the color model used to construct the offscreen image.
     *
     * @return color model
     */
    private static ColorModel createColorModel() {
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        int[] nBits = {8, 8, 8, 8};

        return new ComponentColorModel(cs, nBits, true, false,
                Transparency.TRANSLUCENT,
                DataBuffer.TYPE_BYTE);
    }

    @Override
    public void mouseClicked(MouseEvent e) {

        if (e.getClickCount() == 2) {
            enterFullscreenMode();
        }

//        render();
        contentChanged();
        repaint();

        panel.mouseClicked(e);

    }

    @Override
    public void mousePressed(MouseEvent e) {
        panel.mousePressed(e);
//        render();
        contentChanged();
        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        panel.mouseReleased(e);
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        this.requestFocus();
    }

    @Override
    public void mouseExited(MouseEvent e) {
        panel.mouseMoved(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        panel.mouseMoved(e);
    }

    @Override
    public void mouseDragged(MouseEvent e) {

        panel.mouseDragged(e);
//        render();
        contentChanged();
        repaint();

    }

    @Override
    public void keyTyped(KeyEvent e) {
        panel.keyReleased(e);
//        render();
        contentChanged();
        repaint();
    }

    public void HardCopy(String filename, int mag) {
        panel.HardCopy(filename, mag);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        panel.keyReleased(e);
//        render();
        contentChanged();
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        panel.keyPressed(e);
//        render();
        contentChanged();
        repaint();
    }

    /**
     * Disposes this component.
     */
    public void dispose() {
        panel.Delete();
        window.dispose();
    }

    /**
     * Repairs the visual appearance of this panel.
     */
    private void repair() {
        // This is a hack!
        // I put his method to the bottom because no one should read its code.
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                GraphicsUtil.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        render();
                        repaint();
                    }
                });
            }
        }, 10);
    }

    /**
     * Returns the content alpha value (defines transparency of vtk content).
     *
     * A value of 1.f means full opacity, 0.0f full transparency.
     *
     * @return the content alpha
     */
    public float getContentAlpha() {
        return contentAlpha;
    }

    /**
     * Defines the content alpha value (defines transparency of vtk content).
     *
     * A value of 1.f means full opacity, 0.0f full transparency.
     *
     * @param contentAlpha the content alpha to set
     */
    public void setContentAlpha(float contentAlpha) {
        this.contentAlpha = contentAlpha;
        contentChanged();
    }
}
