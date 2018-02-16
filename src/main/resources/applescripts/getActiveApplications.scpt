tell application "System Events"
	set processList to get every application process whose background only is false
	repeat with proc in processList
		set appPath to POSIX path of application file of proc
		log (appPath)
	end repeat
end tell