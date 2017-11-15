on run argv
	set applicationName to item 1 of argv
	set windowName to item 2 of argv

	set windowFound to false

	tell application "System Events"
		set listOfProcesses to (every process where background only is false)
		repeat with processName in listOfProcesses
			if name of processName is equal to applicationName then
				set listOfWindows to (every window of processName)
				repeat with windowObj in listOfWindows
					log (get name of windowObj)
					if name of windowObj is equal to windowName then
						set the frontmost of processName to true
						perform action "AXRaise" of windowObj
						set windowFound to true
						log "OK"
						return
					end if
				end repeat
			end if
		end repeat

		-- Fallback if the window is not found

		if windowFound is false then
			try
				set appProcess to first application process whose name is equal to applicationName
				set the frontmost of appProcess to true
				log "FALLBACK"
				return
			on error errStr number errorNumber
				log "ERROR"
				return
			end try
		end if
	end tell

	log "ERROR"
end run