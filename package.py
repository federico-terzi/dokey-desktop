import subprocess
import sys
import os
import platform
import click
import glob

#javapackager.exe -deploy -native exe -outdir out -outfile Dokey -srcdir dist -appclass "app.MainLauncher" -name Dokey -title Dokey -v -Bruntime=D:\Downloads\amazon-corretto-8.202.08.2-windows-x64-jre\jre1.8.0_202

@click.group()
def cli():
    pass

@cli.command()
@click.option('--jre', default=None, help='Path to the JRE version to use.')
@click.option('--skip-gradle', '-s', default=False, is_flag=True, help='Avoid the Gradle JAR building phase ( useful when already built )')
@click.option('--name', default="Dokey", help='Name of the target application.')
@click.option('--appclass', default="app.MainLauncher", help='Main class of the application that will be launched first.')
def build(jre, skip_gradle, name, app_class):
    """Build Dokey distribution"""
    # Check operating system
    print("Detected OS:", platform.system())
    GRADLE_PATH = "gradlew"
    PACKAGER_PATH = "javapackager"
    if platform.system() == "Windows":
        GRADLE_PATH = "gradlew.bat"
        PACKAGER_PATH = "javapackager.exe"

    if jre is None:
        print("WARNING: JRE path is not specified, using the system distribution...")
        print("You should specify a specific distribution using the --jre option")
        print("A good distribution is Amazon Corretto, check it out here: https://aws.amazon.com/it/corretto/")
    else:
        print("Using JRE:", jre)

    # Check javapackager
    try:
        subprocess.check_output([PACKAGER_PATH, "-help"])
    except subprocess.CalledProcessError:
        raise Exception("Could not find javapackager, have you added it to the path? Usually it is located into the bin directory of the JDK")

    if not skip_gradle:
        print("STARTING GRADLE BUILING PROGESS")
        print("CLEANING")
        subprocess.run([GRADLE_PATH, "clean"])
        print("BUILDING FAT JAR")
        subprocess.run([GRADLE_PATH, "shadowJar"])
    else:
        print("SKIPPING GRADLE BUILDING PROCESS")

    # Find the application jar
    jar_found = glob.glob("build/libs/*-all.jar")
    if len(jar_found) == 0:
        raise Exception("Could not find JAR, you should probably build it using gradle ( avoid --skip-gradle option )")

    JAR_PATH = os.path.abspath(jar_found[0])
    print("JAR:", JAR_PATH)

    packager_options = ["-deploy", "-outdir", "build/package", "-outfile", name, "-name", name, "-title", name, "-v",
                        "-Bruntime="+jre]

    if platform.system() == "Windows":
        packager_options.append("-native")
        packager_options.append("exe")

    elif platform.system() == "Darwin":  # TODO: mac os
        pass


if __name__ == '__main__':
    print("[[ DOKEY PACKAGER ]]")

    # Check python version 3
    if sys.version_info[0] < 3:
        raise Exception("Must be using Python 3")

    cli()