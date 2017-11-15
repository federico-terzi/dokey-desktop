tell application "System Events"
    set listOfProcesses to (every process where background only is false)
    repeat with processName in listOfProcesses
        set listOfWindows to (every window of processName)
        log (get name of processName)
        log (get unix id of processName)
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
        log ""
    end repeat
end tell