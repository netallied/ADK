# ADK
The AutomationML Development Kit for Java is an API to read and write AutomationML files according to the AutomationML specification Part 1 (October 2014) and the CAEX specification IEC 62424:2008.

Numerous test cases are available to ensure compliance with the AML standard.
For more information see the AutomationML website at www.automationml.org.

## Build instructions
Suggested build environment is eclipse.
* Open eclipse and select [File] -> [Import...]
* Select import wizard "General"/"Existing Projects into Workspace"
* Set the root directory to your cloned repository (e.g. /repos/uber/ADK or C:\\repos\\uber\\ADK)
* Select the projects "de.netallied.adk" and "de.netallied.adk.test" from the projects list
* Close the import wizard with [Finish]
* Select the project de.netallied.adk.test and open the context menu
* Select "Run As"/"JUnit Test"

## Getting started
### Open an AutomationML document
```java
AMLSessionMananger amlSessionManager = new AMLSessionManager();
AMLSession session = amlSessionManager.createSession();
File file = ... // path to the AML document
AMLDocument doc = session.loadAMLDocument(file.toUri().toURL());
```

### Getting an InstanceHierachy by its name and traverse its InternalElements
```java
AMLDocument doc = ...
AMLInstanceHierarchy hierarchy = doc.getInstanceHierarchy("hierarchy");
for (AMLInternalElement element: hierarchy.getInternalElements()) {
    ...
}
```

### Creating an AutomationML document
```java
AMLSessionMananger amlSessionManager = new AMLSessionManager();
AMLSession session = amlSessionManager.createSession();
AMLDocument doc = session.createAMLDocument();
AMLInstanceHierarchy instanceHierarchy = doc.createInstanceHierarchy("hierarchy");
AMLInternalElement internalElement = instanceHierarchy.createInternalElement();
```

### Store an AutomationML document to disk
AutomationML documents may reference other AutomationML documents. When storing them to disk the URLs of the references depend on the location the document is stored. Therefore, an implementation of the interface AMLDocumentURLResolver is needed. A simple resolver looks like this:
```java
class AMLDocumentURLResolverImpl implements AMLDocumentURLResolver {
    @Override
    public boolean isRelative(AMLDocument baseDocument, AMLDocument referencedDocument) {
        return true;
    }

    @Override
    public URL getUrl(AMLDocument document) {
        DocumentLocation documentLocation = document.getDocumentLocation();
        if (documentLocation instanceof URLDocumentLocation) {
            URL url = ((URLDocumentLocation) documentLocation).getUrl();
            return url;
        }
        return null;
    }
}
```
This resolver can be used when storing an AutomationML document to disk.
```java
AMLDocumentURLResolverImpl urlResolver = new AMLDocumentURLResolverImpl();
String filePath = ...
URL url = new File(filePath).toURI().toURL();
document.saveAs(url, urlResolver);
```