# Copyright (C) 2013 jOVAL.org.  All rights reserved.
# This software is licensed under the LGPL 3.0 license available at http://www.gnu.org/licenses/lgpl.txt
#
if ($args.length -eq 2) {
    [String]$Source = $args[0]
    [String]$Output = $args[1]

    $Params = New-Object System.CodeDom.Compiler.CompilerParameters
    $Params.ReferencedAssemblies.Add("System.dll");
    $Params.GenerateExecutable = $false
    $Params.IncludeDebugInformation = $false
    $Params.WarningLevel = 3
    $Params.TreatWarningsAsErrors = $false
    $Params.CompilerOptions = "/optimize"
    $Params.OutputAssembly = $Output
    $Params.GenerateInMemory = $false
    $Temp = $(Get-Item ENV:TEMP).Value
    $Params.TempFiles = New-Object System.CodeDom.Compiler.TempFileCollection $Temp, $true
    
    $Provider = New-Object Microsoft.CSharp.CSharpCodeProvider
    Try {
	$CompilerResults = $Provider.CompileAssemblyFromFile($Params, $Source)
	if ($CompilerResults.Errors.Count -gt 0) {
	    $CodeLines = $(Get-Content $Source) -Split '[\n]'
	    $ErrorMessage = "Compilation Errors:"
	    $errnum = 1;
	    foreach ($CompileError in $CompilerResults.Errors) {
		$ErrorMessage = [String]::Concat($errnum.ToString(), $ErrorMessage, "`n", $CompileError.ToString())
		$errnum++
	    }
	    Write-Error $ErrorMessage
	} else {
	    Write-Output "Compiled successfully to $($Output)"
	}
    } Catch [System.Exception] {
	if ($_.GetType().ToString() -eq "System.Management.Automation.ErrorRecord") {
	    Write-Error $_.Exception.ToString()
	} else {
	    Write-Error $_.ToString()
	}
    }
} else {
    Write-Error "Invalid arguments."
}

