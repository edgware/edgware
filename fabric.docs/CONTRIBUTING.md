# Contributing to Edgware

We welcome contributions, but request that you follow these guidelines.

 - [Raising issues](#raising-issues)
 - [Feature requests](#feature-requests)
 - [Pull-requests](#pull-requests) (including the Edgare [Contributor License Agreement](#contributor-license-agreement))

## <a name="raising-issues"></a>Raising issues

Raise any bug reports on the [mailing list](https://groups.google.com/forum/#!forum/edgware-fabric).
Please be sure to search the list to see if your issue has already been raised.

A good bug report is one that makes it easy for us to understand what you were
trying to do and what went wrong. Provide as much context as possible so that we
can try to recreate the issue.

At a minimum, please include:

 - Version of Edgware - either the release number if you downloaded a zip, or the first few lines of `git log` if you are cloning the repository directly.
 - Version of Java, Mosquitto, and Derby being used.

## <a name="feature-requests"></a>Feature requests

Raise feature requests on the [mailing list](https://groups.google.com/forum/#!forum/edgware-fabric).

## <a name="pull-requests"></a>Pull-Requests

Discuss new features or refactoring of existing code on the
[mailing list](https://groups.google.com/forum/#!forum/edgware-fabric)
before creating a pull-request. There are many features and plug-in points in
Edgware that haven't yet been documented, so it could be that the feature you
are looking for is already available, or easily incorporated via a plug-in.

### <a name="contributor-license-agreement"></a>Contributor License Agreement

In order for us to accept pull-requests, the contributor must first complete
a Contributor License Agreement (CLA). This clarifies the intellectual
property license granted with any contribution. It is for your protection as a
Contributor as well as the protection of IBM, its customers and other users of
Edgware; it does not change your rights to use your own Contributions for any
other purpose.

You can download the CLAs here:

 - [Individual CLA](http://edgware-fabric.org/cla/edgware-cla-individual.pdf)
 - [Corporate CLA](http://edgware-fabric.org/cla/edgware-cla-corporate.pdf)

If you are an IBMer, please contact us directly as the contribution process is
slightly different.

### Coding standards

Please ensure you follow the coding standards used through-out the existing
code base. Some basic rules include:

 - All files must have the Eclipse license in the header.
 - Indent with 4-spaces, no tabs.
 - Opening brace on same line as `if`/`for`/`function` and so on, closing brace
 on its own line.
