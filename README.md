[![Codacy Badge](https://api.codacy.com/project/badge/Grade/67f7afa8d1f449d9b85c3fce51b12a11)](https://www.codacy.com/app/prperiscal/common-collect?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=prperiscal/common-collect&amp;utm_campaign=Badge_Grade)
# Common-Collect


## Overview
Library that includes new collection types, such us DoublyLinkedList.

## Getting Started
* In a Maven .pom file:
```
<dependency>
  <groupId>com.prperiscal</groupId>
  <artifactId>common-collect</artifactId>
  <version>1.0.0-SNAPSHOT</version>
</dependency>
```
Also the repository will be necessary
```
<repositories>
  <repository>
    <id>Pablo-common-collect</id>
    <url>https://packagecloud.io/Pablo/common-collect/maven2</url>
    <releases>  
      <enabled>true</enabled>
    </releases>
    <snapshots>
      <enabled>true</enabled>
    </snapshots>
  </repository>
</repositories>
```

* In a Gradle build.gradle file:
```
compile 'com.prperiscal:common-collect:1.0.0-SNAPSHOT'
```
Also the repository will be necessary
```
repositories {
    maven {
        url "https://packagecloud.io/Pablo/common-collect/maven2"
    }
}
```
## Collections

### Doubly Linked List

A memory efficient version of Doubly Linked List. Instead of having two pointers on each node, only one link is used for storing both pointers.

This class should not be assumed to be universally superior to a common doubly Linked list implementation. Generally speaking, this class reduces object allocation and memory consumption at the price of moderately increased constant factors of CPU.

### (More to come)

## Contributing

Please read [CONTRIBUTING](https://gist.github.com/prperiscal/900729941edc5d5ddaaf9e21e5055a62) for details on our code of conduct, and the process for submitting pull requests to us.

## Workflow

Please read [WORKFLOW-BRANCHING](https://gist.github.com/prperiscal/ce8b8b5a9e0f79378475243e2d227011) for details on our workflow and branching directives. 


## Authors

* **Pablo Rey Periscal** - *Initial work* -

See also the list of [contributors]() who participated in this project.

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details
