Set Shell = CreateObject("WScript.Shell")
Set link = Shell.CreateShortcut(WScript.Arguments(0))
wscript.echo link.TargetPath