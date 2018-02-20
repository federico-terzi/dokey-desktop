Set Shell = CreateObject("WScript.Shell")
Set link = Shell.CreateShortcut(WScript.Arguments(0))
link.TargetPath = WScript.Arguments(1)
link.Arguments = WScript.Arguments(2)
link.Save