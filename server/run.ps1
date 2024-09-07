$sourceFile = "C:\Users\Vítek\IdeaProjects\Computers\target\computers-1.0.jar"
$destinationDir = "C:\Users\Vítek\IdeaProjects\Computers\server\plugins\"

Remove-Item -Path "world" -Recurse -Force

cd plugins

rm computers-1.0.jar

cd ..

if (Test-Path $sourceFile) {
    # Copy the file to the destination directory
    Copy-Item -Path $sourceFile -Destination $destinationDir -Force

    # Output success message
    Write-Output "File copied successfully to $destinationDir"
} else {
    # Output error message
    Write-Output "Source file not found: $sourceFile"
}

java -jar server.jar -nogui
cd ..
Remove-Item -Path "target" -Recurse -Force