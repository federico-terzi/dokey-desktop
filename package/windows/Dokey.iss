;This file will be executed next to the application bundle image
;I.e. current directory will contain folder APPLICATION_NAME with application files
[Setup]

#define PACKAGERPATH GetEnv('PACKAGERPATH')

AppId={{PRODUCT_APP_IDENTIFIER}}
AppName=APPLICATION_NAME
AppVersion=APPLICATION_VERSION
AppVerName=APPLICATION_NAME APPLICATION_VERSION
AppPublisher=APPLICATION_VENDOR
AppComments=APPLICATION_COMMENTS
AppCopyright=APPLICATION_COPYRIGHT
AppPublisherURL=https://dokey.io/
AppSupportURL=https://dokey.io/
AppUpdatesURL=https://dokey.io/
DefaultDirName=APPLICATION_INSTALL_ROOT\APPLICATION_NAME
DisableDirPage=auto
DisableProgramGroupPage=Yes
DisableFinishedPage=Yes
DisableWelcomePage=No
DisableReadyPage=No
DefaultGroupName=APPLICATION_GROUP
;Optional License
LicenseFile=APPLICATION_LICENSE_FILE
;WinXP or above
MinVersion=0,5.1 
OutputBaseFilename=INSTALLER_FILE_NAME
Compression=lzma
SolidCompression=yes
PrivilegesRequired=APPLICATION_INSTALL_PRIVILEGE
SetupIconFile=APPLICATION_NAME\APPLICATION_NAME.ico
UninstallDisplayIcon={app}\APPLICATION_NAME.ico
UninstallDisplayName=APPLICATION_NAME
WizardImageStretch=No
WizardSmallImageFile=APPLICATION_NAME-setup-icon.bmp   
WizardImageFile={#PACKAGERPATH}\Dokey-welcome.bmp
ArchitecturesInstallIn64BitMode=ARCHITECTURE_BIT_MODE

FILE_ASSOCIATIONS

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Messages]
WelcomeLabel2=Thank you for choosing Dokey! Are you ready to supercharge your productivity? This will install [name/ver] on your computer.

[Files]
Source: "APPLICATION_NAME\APPLICATION_NAME.exe"; DestDir: "{app}"; Flags: ignoreversion
Source: "APPLICATION_NAME\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{group}\APPLICATION_NAME"; Filename: "{app}\APPLICATION_NAME.exe"; IconFilename: "{app}\APPLICATION_NAME.ico"; Check: APPLICATION_MENU_SHORTCUT()
Name: "{group}\Dokey Log"; Filename: "{%USERPROFILE}\.dokey\log.txt"
Name: "{commondesktop}\APPLICATION_NAME"; Filename: "{app}\APPLICATION_NAME.exe";  IconFilename: "{app}\APPLICATION_NAME.ico"; Check: APPLICATION_DESKTOP_SHORTCUT()
SECONDARY_LAUNCHERS

[Run]
Filename: "{app}\RUN_FILENAME.exe"; Parameters: "-Xappcds:generatecache"; Check: APPLICATION_APP_CDS_INSTALL()
Filename: "{app}\RUN_FILENAME.exe"; Description: "{cm:LaunchProgram,APPLICATION_NAME}"; Flags: nowait postinstall skipifsilent; Check: APPLICATION_NOT_SERVICE()
Filename: "{app}\RUN_FILENAME.exe"; Parameters: "-install -svcName ""APPLICATION_NAME"" -svcDesc ""APPLICATION_DESCRIPTION"" -mainExe ""APPLICATION_LAUNCHER_FILENAME"" START_ON_INSTALL RUN_AT_STARTUP"; Check: APPLICATION_SERVICE()

[UninstallRun]
Filename: "{cmd}"; Parameters: "/C ""taskkill /im Dokey.exe /f /t"
Filename: "{cmd}"; Parameters: "/C ""taskkill /im adb.exe /f /t"
Filename: "{app}\RUN_FILENAME.exe "; Parameters: "-uninstall -svcName APPLICATION_NAME STOP_ON_UNINSTALL"; Check: APPLICATION_SERVICE()

[Code]
function returnTrue(): Boolean;
begin
  Result := True;
end;

function returnFalse(): Boolean;
begin
  Result := False;
end;

function InitializeSetup(): Boolean;
begin
// Possible future improvements:
//   if version less or same => just launch app
//   if upgrade => check if same app is running and wait for it to exit
//   Add pack200/unpack200 support? 
  Result := True;
end;  