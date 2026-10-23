# WakeSpeed 一键发布到 GitHub（支持覆盖已存在文件）
# 用法: .\publish.ps1 -Token "ghp_xxx"
param(
    [Parameter(Mandatory = $true)][string]$Token,
    [string]$RepoName = "WakeSpeed",
    [string]$Description = "Minecraft 赛车船 HUD + 引擎/电机音效 + 赛车视角 | Racing-boat HUD, engine & EV sound, racing camera - Fabric 26.1.2 / 26.2",
    [switch]$Private
)

$ErrorActionPreference = "Stop"
$repoDir = "C:\Users\Administrator\WakeSpeed-repo"
$api = "https://api.github.com"
$headers = @{
    "Authorization"        = "Bearer $Token"
    "Accept"               = "application/vnd.github+json"
    "X-GitHub-Api-Version" = "2022-11-28"
    "User-Agent"           = "WakeSpeed-Publisher"
}

function Invoke-GH($method, $path, $body) {
    $params = @{ Method = $method; Uri = "$api$path"; Headers = $headers; TimeoutSec = 90 }
    if ($body -ne $null) {
        $params.Body = ($body | ConvertTo-Json -Depth 10 -Compress)
        $params.ContentType = "application/json; charset=utf-8"
    }
    return Invoke-RestMethod @params
}

Write-Host "[1/5] 验证令牌..." -ForegroundColor Cyan
$me = Invoke-GH "GET" "/user" $null
$owner = $me.login
Write-Host "      登录账号: $owner" -ForegroundColor Green

Write-Host "[2/5] 检查仓库 $owner/$RepoName ..." -ForegroundColor Cyan
$branch = "main"
try {
    $info = Invoke-GH "GET" "/repos/$owner/$RepoName" $null
    $branch = $info.default_branch
    Write-Host "      仓库已存在，默认分支: $branch" -ForegroundColor Yellow
} catch {
    $null = Invoke-GH "POST" "/user/repos" @{
        name = $RepoName; description = $Description; private = [bool]$Private
        has_issues = $true; has_wiki = $false; auto_init = $false
    }
    Write-Host "      创建成功" -ForegroundColor Green
}

Write-Host "[3/5] 扫描文件..." -ForegroundColor Cyan
$files = Get-ChildItem $repoDir -Recurse -File | Where-Object {
    $_.FullName -notmatch '\\\.git\\' -and $_.Name -ne 'publish.ps1'
}
Write-Host "      待上传 $($files.Count) 个文件" -ForegroundColor Green

Write-Host "[4/5] 上传（已存在的自动覆盖更新）..." -ForegroundColor Cyan
$ok = 0; $updated = 0; $fail = 0
$base = (Resolve-Path $repoDir).Path.TrimEnd('\')
foreach ($f in $files) {
    $rel = $f.FullName.Substring($base.Length + 1) -replace '\\', '/'
    $content = [Convert]::ToBase64String([IO.File]::ReadAllBytes($f.FullName))

    $sha = $null
    try {
        $existing = Invoke-GH "GET" "/repos/$owner/$RepoName/contents/$rel`?ref=$branch" $null
        $sha = $existing.sha
    } catch { }

    $body = @{
        message = if ($sha) { "Update $rel" } else { "Add $rel" }
        content = $content
        branch  = $branch
    }
    if ($sha) { $body["sha"] = $sha }

    try {
        $null = Invoke-GH "PUT" "/repos/$owner/$RepoName/contents/$rel" $body
        if ($sha) {
            $updated++
            Write-Host "      ~ $rel (更新)" -ForegroundColor Yellow
        } else {
            $ok++
            Write-Host "      + $rel" -ForegroundColor DarkGray
        }
    } catch {
        $fail++
        Write-Host "      ! $rel -> $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "[5/5] 完成" -ForegroundColor Cyan
Write-Host "      新增 $ok / 更新 $updated / 失败 $fail" -ForegroundColor Green
Write-Host ""
Write-Host "仓库地址: https://github.com/$owner/$RepoName" -ForegroundColor Cyan

if ($fail -gt 0) { exit 1 }
