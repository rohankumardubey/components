---
version: 7.1.1
module: https://talend.poolparty.biz/coretaxonomy/42
product:
- https://talend.poolparty.biz/coretaxonomy/23
---

# TPS-3151

| Info             | Value |
| ---------------- | ---------------- |
| Patch Name       | Patch\_20190605\_TPS-3151\_v1\-7.1.1 |
| Release Date     | 2019-06-05 |
| Target Version   | Talend-Studio-20181026_1147-V7.1.1 |
| Product affected | Talend Studio |

## Introduction

This patch is cumulative. It includes all previous generally available patches of Salesforce for Talend Studio 7.1.1.

**NOTE**: For information on how to obtain this patch, reach out to your Support contact at Talend.

## Fixed issues

This patch contains the following fixes:

- TPS-3151 [7.1.1]Salesforce Oauth: support additional endpoint (aud, iss, ...) for JWT flow(TDI-40997)

## Prerequisites

Consider the following requirements for your system:

- Talend Studio 7.1.1 must be installed.
- remove the folder "{studio}/configuration/org.eclipse.osgi".

## Installation

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

## Uninstallation <!-- if applicable -->

<!--
Detailed instructions to uninstall the patch

In case this patch cannot be uninstalled, it is your responsability to define the backup procedures for your organization before installing.

-->
Backup the Affected files list below. Uninstall the patch by restore the backup files.

## Affected files for this patch <!-- if applicable -->

The following files are installed by this patch:

- {Talend_Studio_path}/configuration/.m2/repository/org/talend/components/components\-salesforce\-definition/0.25.3/components\-salesforce\-definition\-0.25.3.jar
- {Talend_Studio_path}/configuration/.m2/repository/org/talend/components/components\-salesforce\-runtime/0.25.3/components\-salesforce\-runtime\-0.25.3.jar
- {Talend_Studio_path}/configuration/.m2/repository/org/talend/components/components\-common\-oauth/0.25.3/components\-common\-oauth\-0.25.3.jar
- {Talend_Studio_path}/org.talend.components.common\-oauth_0.25.3.jar
- {Talend_Studio_path}/org.talend.components.salesforce.definition_0.25.3.jar
