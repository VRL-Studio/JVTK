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

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Window;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;


/**
 * Utility class that simplifies UI related tasks such as enter/leave fullscreen.
 *
 * @author Michael Hoffer <info@michaelhoffer.de>
 */
public class GraphicsUtil {

    // no instanciation allowed
    private GraphicsUtil() {
        throw new AssertionError(); // not in this class either!
    }

    /**
     * Returns a graphics device.
     *
     * @param deviceID the device id
     * @return the device
     */
    public static GraphicsDevice getGraphicsDevice(int deviceID) {
        GraphicsEnvironment graphicsEnvironment =
                GraphicsEnvironment.getLocalGraphicsEnvironment();

        GraphicsDevice[] devices =
                graphicsEnvironment.getScreenDevices();

        return devices[deviceID];
    }

    /**
     * Returns the screen ID of the specified window. This method can be used to
     * find out on which screen the specified window is displayed.
     *
     * @return the screen id or
     * <code>-1</code> if no screen can be found
     */
    public static int getScreenId(Window w) {
        int result = -1;

        GraphicsDevice d = w.getGraphicsConfiguration().getDevice();

        GraphicsEnvironment graphicsEnvironment =
                GraphicsEnvironment.getLocalGraphicsEnvironment();

        GraphicsDevice[] devices =
                graphicsEnvironment.getScreenDevices();

        for (int i = 0; i < devices.length; i++) {
            if (d.equals(devices[i])) {
                result = i;
                break;
            }
        }

        return result;
    }

    /**
     * Enters fullscreen mode. <p><b>Warning:</b> using native support may cause
     * problems with multi screen setups on OS X.</p>
     *
     * @param frame
     * @param screenID id of the target screen
     * @param nativeSupport defines whether to use tative fullscreen support
     */
    public static void enterFullscreenMode(final Window frame, int screenID,
            boolean nativeSupport) {
        GraphicsDevice graphicsDevice = getGraphicsDevice(screenID);

        if (graphicsDevice.isFullScreenSupported() && nativeSupport && SysUtil.isMacOSX()) {

            if (frame instanceof JFrame) {
                ((JFrame) frame).setResizable(false);
            }

            // enter fullscreen mode
            graphicsDevice.setFullScreenWindow(frame);
            frame.validate();

        } else {
            Rectangle bounds =
                    graphicsDevice.getDefaultConfiguration().getBounds();
            frame.setVisible(false);
            frame.setBounds(bounds);
            frame.setVisible(true);
//            frame.setBounds(bounds);
            if (nativeSupport) {
                System.err.println("Fullscreen mode not supported");
            }
        }//end else
    }

    /**
     * Enters fullscreen mode.
     *
     * @param frame
     */
    public static void enterFullscreenMode(final Window frame) {
        enterFullscreenMode(frame, 0, true);
    }

    /**
     * Leaves fullscreen mode. This method should only be used if native support
     * requested and/or supported.
     *
     * @param frame
     */
    public static void leaveFullscreenMode(Window frame) {
        GraphicsDevice graphicsDevice = getGraphicsDevice(getScreenId(frame));

        if (graphicsDevice.isFullScreenSupported()) {
            // Enter full-screen mode with an undecorated,
            // non-resizable JFrame object.
//            frame.setVisible(false);
//            frame.setUndecorated(false);


            if (frame instanceof JFrame) {
                ((JFrame) frame).setResizable(true);
            }
//            frame.setVisible(true);
            //Make it happen!
            graphicsDevice.setFullScreenWindow(null);
            frame.validate();

        } else {
            System.out.println("Fullscreen mode not supported");
        }//end else
    }
    
    public static void invokeAndWait(final Runnable r) {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {

                    @Override
                    public void run() {
                        r.run();
                    }
                });
            } catch (InterruptedException ex) {
                Logger.getLogger(GraphicsUtil.class.getName()).
                        log(Level.SEVERE, null, ex);
            } catch (InvocationTargetException ex) {
                Logger.getLogger(GraphicsUtil.class.getName()).
                        log(Level.SEVERE, null, ex);
            }
        }
    }

    public static void invokeLater(final Runnable r) {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    r.run();
                }
            });
        }
    }
}
