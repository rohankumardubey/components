---
version: 7.1.1
module: https://talend.poolparty.biz/coretaxonomy/42
product:
- https://talend.poolparty.biz/coretaxonomy/23
---

# TPS-4782 <!-- mandatory -->

| Info             | Value |
| ---------------- | ---------------- |
| Patch Name       | Patch\_20210513\_TPS-4782\_v1-7.1.1 |
| Release Date     | 2021-05-13 |
| Target Version   | 20181026\_1147-V7.1.1 |
| Product affected | Talend Studio |

## Introduction <!-- mandatory -->

This is a self-contained patch of Snowflake component for 7.1.1.

**NOTE**: For information on how to obtain this patch, reach out to your Support contact at Talend.

## Fixed issues <!-- mandatory -->

This patch for 7.1.1 contains the following fixes:

- TDI-44413 The tDBOutputBluk does not load date correctly

## Prerequisites <!-- mandatory -->

Consider the following requirements for your system:

- Talend Studio 7.1.1 must be installed.
- TPS-3363 (cumulative monthly patch) or later.
- Installed patch for Snowflake 0.25.5 version from Components Manager.

## Installation <!-- mandatory -->
### Installing the patch using Talend Studio
From the Talend Studio 7.1.1 installation folder, make a copy of the following files somewhere safe:
- {Talend_Studio_path}/configuration/.m2/repository/org/talend/components/components-common/0.25.5/components-common-0.25.5.jar
- {Talend_Studio_path}/configuration/.m2/repository/org/talend/components/components-snowflake-ee-runtime/0.25.5/components-snowflake-ee-runtime-0.25.5.jar
- {Talend_Studio_path}/configuration/.m2/repository/org/talend/components/components-snowflake-runtime/0.25.5/components-snowflake-runtime-0.25.5.jar

Unzip content of the patch zip onto your Talend Studio 7.1.1 folder.

## Uninstallation

Replace the files overridden by the patch by the copy you made before unzipping.

## Affected files for this patch

The following files are installed by this patch:
- {Talend_Studio_path}/configuration/.m2/repository/org/talend/components/components-common/0.25.5/components-common-0.25.5.jar
- {Talend_Studio_path}/configuration/.m2/repository/org/talend/components/components-snowflake-ee-runtime/0.25.5/components-snowflake-ee-runtime-0.25.5.jar
- {Talend_Studio_path}/configuration/.m2/repository/org/talend/components/components-snowflake-runtime/0.25.5/components-snowflake-runtime-0.25.5.jar
