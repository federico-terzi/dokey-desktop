import subprocess
import sys
import os
import platform
import click
import glob
import shutil

#javapackager.exe -deploy -native exe -outdir out -outfile Dokey -srcdir dist -appclass "app.MainLauncher" -name Dokey -title Dokey -v -Bruntime=D:\Downloads\amazon-corretto-8.202.08.2-windows-x64-jre\jre1.8.0_202

@click.group()
def cli():
    pass

@cli.command()
@click.option('--jre', default=None, help='Path to the JRE version to use.')
@click.option('--skip-gradle', '-s', default=False, is_flag=True, help='Avoid the Gradle JAR building phase ( useful when already built )')
@click.option('--name', default="Dokey", help='Name of the target application.')
@click.option('--appclass', default="app.MainLauncher", help='Main class of the application that will be launched first.')
@click.option('--id', default="com.rocketguys.Dokey", help='The identifier of the app, as reverse DNS order ( such as com.example.app )')
@click.option('--vendor', default="Rocket Guys", help='Vendor of the app')
def build(jre, skip_gradle, name, appclass, id, vendor):
    """Build Dokey distribution"""
    # Check operating system
    GRADLE_PATH = "gradlew"
    PACKAGER_PATH = "javapackager"
    TARGET_OS = "macosx"
    if platform.system() == "Windows":
        GRADLE_PATH = "gradlew.bat"
        PACKAGER_PATH = "javapackager.exe"
        TARGET_OS = "windows"

    print("Detected OS:", TARGET_OS)

    if jre is None:
        print("WARNING: JRE path is not specified, using the system distribution...")
        print("You should specify a specific distribution using the --jre option")
        print("A good distribution is Amazon Corretto, check it out here: https://aws.amazon.com/it/corretto/")
    else:
        print("Using JRE:", jre)

    # Read Dokey version
    PROPERTY_FILE = "src/main/resources/proj.properties"
    VERSION = None
    with open(PROPERTY_FILE, "r") as pf:
        for line in pf.readlines():
            if line.startswith("version"):
                VERSION = line.split("=")[1].strip()
    print("VERSION:", VERSION)

    # Check javapackager
    try:
        subprocess.run([PACKAGER_PATH, "-help"], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
    except FileNotFoundError:
        raise Exception("Could not find javapackager, have you added it to the path? Usually it is located into the bin directory of the JDK")

    # Check Inno Setup
    if TARGET_OS == "windows":
        try:
            subprocess.run(["iscc"], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
        except FileNotFoundError:
            raise Exception("Could not find Inno Setup, you can download it here ( unicode version is better ): http://www.jrsoftware.org/isdl.php")


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
    _, JAR_NAME = os.path.split(JAR_PATH)
    print("JAR:", JAR_NAME)

    TMP_DIR = "build/tmp/packager"
    OUTPUT_PATH = "build/package"

    # Clearing previous build directory
    if os.path.isdir(TMP_DIR):
        print("Cleaning packager temp directory...")
        shutil.rmtree(TMP_DIR)
    if os.path.isdir(OUTPUT_PATH):
        print("Cleaning packager output directory...")
        shutil.rmtree(OUTPUT_PATH)
    os.makedirs(TMP_DIR, exist_ok=True)

    # Create packager directory
    print("Copying JAR...")
    shutil.copyfile(JAR_PATH, os.path.join(TMP_DIR, JAR_NAME))

    print("Copying resources...")
    exclude_targets = []
    if TARGET_OS == "windows":
        exclude_targets.append("mac")
    elif TARGET_OS == "macosx":
        exclude_targets.append("win")

    for directory in glob.glob("src/main/resources/*"):
        _, dir_name = os.path.split(directory)
        if dir_name not in exclude_targets and os.path.isdir(directory):
            print("--> Copying", dir_name, "...")
            shutil.copytree(directory, os.path.join(TMP_DIR, dir_name))

    packager_options = ["-deploy", "-outdir", OUTPUT_PATH, "-outfile", name, "-name", name, "-title", name, "-v",
                        "-srcdir", TMP_DIR, "-appclass", appclass, "-BappVersion="+VERSION, "-Bidentifier="+id,
                        "-Bvendor="+vendor]

    # Specify JRE version if needed
    if jre is not None:
        packager_options.append("-Bruntime="+jre)

    if TARGET_OS == "windows":
        packager_options.append("-native")
        packager_options.append("exe")

    elif TARGET_OS == "macosx":  # TODO: mac os
        pass

    print("Launching javapackager with parameters:", packager_options)
    _packager_options = ["javapackager"]
    _packager_options.extend(packager_options)
    my_env = os.environ.copy()
    my_env["PACKAGERPATH"] = os.path.abspath(os.path.join("package", TARGET_OS))
    subprocess.run(_packager_options, env=my_env)

    # TODO: copy needed resources, jar and package files in a temp directory and then run java packager
    # TODO: code signing


if __name__ == '__main__':
    print("[[ DOKEY PACKAGER ]]")

    # Check python version 3
    if sys.version_info[0] < 3:
        raise Exception("Must be using Python 3")

    cli()