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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;
import vtk.vtkNativeLibrary;

/**
 *
 * @author Michael Hoffer <info@michaelhoffer.de>
 */
public class SysUtil {

    public static final String OS_LINUX = "Linux";
    public static final String OS_MAC = "Mac OS X";
    public static final String OS_WINDOWS = "Windows";
    public static final String OS_OTHER = "Other";
    public static final String[] SUPPORTED_OPERATING_SYSTEMS = {OS_LINUX, OS_MAC, OS_WINDOWS};

    /**
     * Loads native vtk libraries from the specified path.
     *
     * @param path path where the native vtk libraries are located
     */
    public static void loadLibraries(String path) {
        try {
            SysUtil.addNativeLibraryPath(path);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (!vtkNativeLibrary.LoadAllNativeLibraries()) {
            for (vtkNativeLibrary lib : vtkNativeLibrary.values()) {
                if (!lib.IsLoaded()) {
                    System.out.println(lib.GetLibraryName() + " not loaded");
                }
            }

            System.out.println("Make sure the search path is correct: ");
            System.out.println(System.getProperty("java.library.path"));
        }

        vtkNativeLibrary.DisableOutputWindow(null);
    }

    /**
     * Adds a folder path to the native library path.
     *
     * @param path path to add
     * @throws IOException
     */
    private static void addNativeLibraryPath(String path) throws IOException {
        try {
            // This enables the java.library.path to be modified at runtime
            // Idea comes from a Sun engineer at
            // http://forums.sun.com/thread.jspa?threadID=707176
            //
            Field field = ClassLoader.class.getDeclaredField("usr_paths");
            field.setAccessible(true);
            String[] paths = (String[]) field.get(null);
            for (int i = 0; i < paths.length; i++) {
                if (path.equals(paths[i])) {
                    return;
                }
            }
            String[] tmp = new String[paths.length + 1];
            System.arraycopy(paths, 0, tmp, 0, paths.length);
            tmp[paths.length] = path;
            field.set(null, tmp);
            System.setProperty("java.library.path",
                    System.getProperty("java.library.path")
                    + File.pathSeparator + path);
        } catch (IllegalAccessException e) {
            throw new IOException("Failed to get permissions to set library path");
        } catch (NoSuchFieldException e) {
            throw new IOException("Failed to get field handle to set library path");
        }
    }

    /**
     * <p> Returns the OS name. If the OS is not supported, "Other" will be
     * returned. </p> <p> <b>Note:</b> in contrary to
     * <code>System.getProprty()</code> only the base name will be returned. See {@link #SUPPORTED_OPERATING_SYSTEMS}.
     * </p>
     *
     * @return the OS name
     */
    public static String getOS() {
        String result = OS_OTHER;

        String osName = System.getProperty("os.name");

        for (String s : SUPPORTED_OPERATING_SYSTEMS) {
            if (osName.contains(s)) {
                result = s;
                break;
            }
        }

        return result;
    }

    public static boolean isWindows() {
        return getOS().equals(OS_WINDOWS);
    }

    public static boolean isMacOSX() {
        return getOS().equals(OS_MAC);
    }

    public static boolean isLinux() {
        return getOS().equals(OS_LINUX);
    }
}
