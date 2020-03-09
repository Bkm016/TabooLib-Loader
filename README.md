# TabooLib-Loader

## Usage
Insert this snippet into your `build.gradle`
```groovy
repositories {
    maven { url "http://repo.ptms.ink/repository/maven-releases/" }
}

dependencies {
    compile 'io.izzel.taboolib:TabooLibLoader:1.7:all'
}

shadowJar {
    dependencies {
        include dependency('io.izzel.taboolib.loader:.*')
    }
    relocate 'io.izzel.taboolib.loader', project.group
}
```
