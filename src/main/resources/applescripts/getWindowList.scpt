tell application "System Events"
    set listOfProcesses to (every process where background only is false)
    repeat with processName in listOfProcesses
        set listOfWindows to (every window of processName)
        log (get name of processName)
        log (get unix id of processName)
        repeat with windowObj in listOfWindows
            try
                log (get name of windowObj)
            on error
            end try
        end repeat
        log ""
    end repeat
end tell