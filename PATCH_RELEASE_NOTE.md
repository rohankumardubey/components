---
version: 7.1.1
module: https://talend.poolparty.biz/coretaxonomy/42
product:
- https://talend.poolparty.biz/coretaxonomy/23
---

# TPS-3366

| Info             | Value |
| ---------------- | ---------------- |
| Patch Name       | Patch\_20190826\_TPS-3366\_v1-7.1.1 |
| Release Date     | 2019-08-26 |
| Target Version   | 20181026_1147-V7.1.1 |
| Product affected | Talend Studio |

## Introduction

This patch is cumulative.

**NOTE**: For information on how to obtain this patch, reach out to your Support contact at Talend.

## Fixed issues

This patch contains the following fixes:

- TPS-3366 [7.1.1] tJDBCOutput does not work with BLOB dataType (TDI-42747)
- TPS-3316 [7.1.1]The data exceeds the max capacity for the data type value = 'null' even though it is not null(TDI-41611)

## Prerequisites

Consider the following requirements for your system:

- Talend Studio 7.1.1 must be installed.

## Installation

**NOTE**: If the patch is deployed in the apporach **Installing the patch using Talend Studio**, the folder **configuration** under this patch must be replaced manually.

### Installing the patch using Software update

1) Logon TAC and switch to Configuration->Software Update, then enter the correct values and save referring to the documentation: https://help.talend.com/reader/f7Em9WV_cPm2RRywucSN0Q/j9x5iXV~vyxMlUafnDejaQ

2) Switch to Software update page, where the new patch will be listed. The patch can be downloaded from here into the nexus repository.

3) On Studio Side: Logon Studio with remote mode, on the logon page the Update button is displayed: click this button to install the patch.

### Installing the patch using Talend Studio

1) Create a folder named "patches" under your studio installer directory and copy the patch .zip file to this folder.

2) Restart your studio: a window pops up, then click OK to install the patch, or restart the commandline and the patch will be installed automatically.

### Installing the patch using Commandline

Execute the following commands:

1. Talend-Studio-win-x86_64.exe -nosplash -application org.talend.commandline.CommandLine -consoleLog -data commandline-workspace startServer -p 8002 --talendDebug
2. initRemote {tac_url} -ul {TAC login username} -up {TAC login password}
3. checkAndUpdate -tu {TAC login username} -tup {TAC login password}
