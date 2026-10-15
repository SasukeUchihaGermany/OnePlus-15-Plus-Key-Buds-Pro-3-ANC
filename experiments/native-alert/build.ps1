param([string]$Sdk="$env:LOCALAPPDATA\Android\Sdk",[string]$Jdk='C:\Program Files\Android\Android Studio\jbr')
$ErrorActionPreference='Stop'
$project=$PSScriptRoot
$build="$project\.build"
$env:JAVA_HOME=$Jdk
$env:Path="$Jdk\bin;$env:Path"
$bt="$Sdk\build-tools\36.1.0"
$android="$Sdk\platforms\android-36\android.jar"
New-Item -ItemType Directory -Force "$build\classes","$build\dex","$project\.local" | Out-Null
function Check {if($LASTEXITCODE -ne 0){throw "Build failed $LASTEXITCODE"}}
& "$Jdk\bin\javac.exe" -encoding UTF-8 -source 8 -target 8 -classpath $android -d "$build\classes" (Get-ChildItem "$project\src" -Recurse -Filter '*.java').FullName
Check
& "$Jdk\bin\jar.exe" cf "$build\classes.jar" -C "$build\classes" .
Check
& "$bt\d8.bat" --lib $android --min-api 36 --output "$build\dex" "$build\classes.jar"
Check
& "$bt\aapt2.exe" link -I $android --manifest "$project\AndroidManifest.xml" -o "$build\unsigned.apk"
Check
Copy-Item "$build\dex\classes.dex" "$build\classes.dex" -Force
Push-Location $build
try{& "$Jdk\bin\jar.exe" uf unsigned.apk classes.dex;Check}finally{Pop-Location}
& "$bt\zipalign.exe" -f 4 "$build\unsigned.apk" "$build\aligned.apk"
Check
$key="$project\.local\probe.p12"
if(!(Test-Path $key)){& "$Jdk\bin\keytool.exe" -genkeypair -keystore $key -storepass android -keypass android -alias probe -dname 'CN=Local Native Alert Probe' -keyalg RSA -validity 3650;Check}
& "$bt\apksigner.bat" sign --ks $key --ks-pass pass:android --out "$project\AncNativeProbe.apk" "$build\aligned.apk"
Check
& "$bt\apksigner.bat" verify "$project\AncNativeProbe.apk"
Check
