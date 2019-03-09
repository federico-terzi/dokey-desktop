# A simple converter from Photoshop Scripting Listener JS to code to be injected
print("Paste Photoshop Code and then enter a blank line")
lines = []
while True:
    line = input(">")
    lines.append(line.strip())
    if line == "":
        break

print(''.join(lines))