JVTK
====

Swing based [VTK](http://www.vtk.org/) Panel (supports transparency and fullscreen mode).

Webpage: http://miho.github.com/JVTK

## Why this project?

The Java binding of VTK only provides heavyweight AWT based UI components. This has been done due to performance
issues when accessing the native render data from Java. In some cases however, it is necessary to use real Swing components
(lightweight). This allows for component layers (components can be rendered on top of the VTK component),
transparency and many other features.

This project addresses the performance issue with custom color and sample model which allow for direct image conversion.
Although not optimal, this is relatively efficient for small component sizes (up to 800x600). In addition to that a
fullscreen mode has been implemented to gain full render performance.

## Dependencies

- VTK 5.10
- C++ Compiler, CMake (see [VTK documentation](http://www.vtk.org/Wiki/VTK#Building_VTK) for details)
- Java 1.6
- Netbeans 7.1 (optional)

## Compile

- Build and install VTK with the following options:

```
BUILD_SHARED_LIBS=ON
BUILD_TESTING=OFF
VTK_WRAP_JAVA=ON
CMAKE_BUILD_TYPE=Release
```

- Copy the content of `$CMAKE_INSTALL_PREFIX/vtk-5.10/bin` to `JVTK/natives` (The default install location depends
  on the VTK version and OS). It is suggested to manually change the install prefix, e.g., `/home/user/vtk`)
  > **NOTE**
  > On Windows it is necessary to copy the runtime dependencies to the `JVTK/natives` folder as well, even if they are
  > present in the `C:\Windows\System32` folder. If VTK has been compiled with Visual Studio 2010 at least the
  > runtime files `msvcr100.dll` and `msvcp100.dll` must be deployed.
  > See [Deploying a Visual C++ Application](http://msdn.microsoft.com/en-us/library/dd293565.aspx) for details.

- Open the `JVTK` project with NetBeans and compile (necessary preferences are already defined)


## Usage

- Open the `JVTK` project with NetBeans and run it.
- Press the `Increase Alpha` and `Decrease Alpha` buttons to change transparency
- Double-Click to enter fullscreen mode
- Double-Click again or press `ESC` to leave fullscreen mode

> **NOTE**
> In fullscreen mode rendering is done with maximum performance as native rendering is used.