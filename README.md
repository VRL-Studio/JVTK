JVTK
====

Swing based [VTK](http://www.vtk.org/) Panel (supports transparency and fullscreen mode).

Webpage: http://miho.github.com/JVTK

## Dependencies

- VTK 5.10
- Java 1.6
- Netbeans 7.1 (optional)

## Compile

- Build VTK with the following options:

```
BUILD_SHARED_LIBS=ON
BUILD_TESTING=OFF
VTK_WRAP_JAVA=ON
CMAKE_BUILD_TYPE=Release
```

- Copy the content of `$VTKBUILD/bin` to `JVTK/natives` (`$VTKBUILD` is the location of the VTK build folder)

- Open the `JVTK` project with NetBeans and compile (necessary preferences are already defined)


## Usage

- Open the `JVTK` project with NetBeans and run it.
- Press the `Increase Alpha` and `Decrease Alpha` buttons to change transparency
- Double-Click to enter fullscreen mode
- Double-Click again or press `ESC` to leave fullscreen mode

> **NOTE**
> In fullscreen mode rendering is done with maximum performance as native rendering is used.