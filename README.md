Java library to work with AR arhives. For more info about format please consult:
http://en.wikipedia.org/wiki/Ar_%28Unix%29


Features
========

  * input and output stream
  * support for GNU long file names

Maven support
========

Add dependency:
```
<dependency>
	<groupId>com.google.code</groupId>
	<artifactId>ar</artifactId>
	<version>2.2</version>
</dependency>
```

Read
========

```
InputStream is = ...
ArInputStream aris = null;
try {
	aris = new ArInputStream(is);
	ArEntry curEntry = null;
	while( (curEntry = aris.getNextEntry()) != null ) {
	    //process entry
	}
} catch(Exception e) {
	//do logging. handle exception
} finally {
	if( aris != null ) {
	    try {
	        aris.close();
	    } catch(IOException e) {
	        //do logging
	    }
	}
}
```

Write
========

```
OutputStream os = ...
ArEntry[] entries = ...
ArOutputStream aros = null;
try {
	aros = new ArOutputStream(os);
	aros.setEntries(entries);
} catch(Exception e) {
	//do logging. handle exception
} finally {
	if( aros != null ) {
	    try {
	        aros.close();
	    } catch(IOException e) {
	        //do logging
	    }
	}
}
```
