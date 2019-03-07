import subprocess
import sys
import os
import platform
import click

#javapackager.exe -deploy -native exe -outdir out -outfile Dokey -srcdir dist -appclass "app.MainLauncher" -name Dokey -title Dokey -v -Bruntime=D:\Downloads\amazon-corretto-8.202.08.2-windows-x64-jre\jre1.8.0_202

@click.group()
def cli():
    pass

@cli.command()
@click.option('--jre', default=None, help='Path to the JRE version to use.')
@click.option('--skip-gradle', '-s', default=False, is_flag=True, help='Avoid the Gradle JAR building phase ( useful when already built )')
def build(jre, skip_gradle):
    """Build Dokey distribution"""
    # Check operating system
    print("Detected OS:", platform.system())
    GRADLE_PATH = "gradlew"
    if platform.system() == "Windows":
        GRADLE_PATH = "gradlew.bat"

    if jre is None:
        print("WARNING: JRE path is not specified, using the system distribution...")
        print("You should specify a specific distribution using the --jre option")
        print("A good distribution is Amazon Corretto, check it out here: https://aws.amazon.com/it/corretto/")
    else:
        print("Using JRE:", jre)

    if not skip_gradle:
        print("STARTING GRADLE BUILING PROGESS")
        print("CLEANING")
        subprocess.run([GRADLE_PATH, "clean"])
        print("BUILDING FAT JAR")
        subprocess.run([GRADLE_PATH, "shadowJar"])
    else:
        print("SKIPPING GRADLE BUILDING PROCESS")

    # Find the application jar
    JAR_DIR = os.path.join("build", "libs")
    if not os.path.isdir(JAR_DIR):
        raise Exception("Could not find JAR directory, you should probably build it using gradle ( avoid --skip-gradle option )")


if __name__ == '__main__':
    print("[[ DOKEY PACKAGER ]]")

    # Check python version 3
    if sys.version_info[0] < 3:
        raise Exception("Must be using Python 3")

    cli()