# Trabalho 2 de Compiladores
Professor Daniel Lucrédio <br>
30 de Abril de 2025

Integrantes:
- Danilo da Silva Pinto, 811537
- Lucas de Araujo Cardoso, 813583
- Pedro Vinícius Guandalini Vicente, 812124

## Requisitos para o Adequado Funcionamento do Sistema:

Para o correto funcionamento do programa, é necessário o JDK versão 17, como pedido no arquivo pom.xml:
```
<properties>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
</properties>
```

A sua versão do JDK pode ser conferida conforme o comando abaixo:
```
java --version
openjdk 17.0.14 2025-01-21
OpenJDK Runtime Environment (build 17.0.14+7-Ubuntu-122.04.1)
OpenJDK 64-Bit Server VM (build 17.0.14+7-Ubuntu-122.04.1, mixed mode, sharing)
```

Também é necessário o Maven em versão compatível:
```
mvn --version
Apache Maven 3.9.8 (36645f6c9b5079805ea5009217e36f2cffd34256)
Maven home: /home/pedroubuntu/coisasApacheMaven/apache-maven-3.9.8-bin/maven
Java version: 17.0.14, vendor: Ubuntu, runtime: /usr/lib/jvm/java-17-openjdk-amd64
Default locale: en_US, platform encoding: UTF-8
OS name: "linux", version: "5.15.0-136-generic", arch: "amd64", family: "unix"
```



## Compilação e Geração dos Pacotes:

Após clonar o repositório:
```
git clone https://github.com/danilosp1/Trabalhos-Compiladores.git
```
Deve-se entrar para dentro dele e utilizar o comando abaixo, obtendo um "BUILD SUCCES", semelhante ao listado.
Além disso, esse comando também deve gerar o arquivo "NossaLexica-1.0-SNAPSHOT.jar" na pasta "target".
```
mvn clean package
```
```
[INFO] Scanning for projects...
[INFO] 
[INFO] ---------------< br.ufscar.dc.compiladores:NossaLexica >----------------
[INFO] Building NossaLexica 1.0-SNAPSHOT
[INFO]   from pom.xml
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- clean:3.2.0:clean (default-clean) @ NossaLexica ---
[INFO] Deleting /Users/danilosp/Desktop/Folder/Faculdade/Compiladores/T2/Compiladores-T2/target
[INFO] 
[INFO] --- antlr4:4.13.2:antlr4 (antlr) @ NossaLexica ---
[INFO] ANTLR 4: Processing source directory /Users/danilosp/Desktop/Folder/Faculdade/Compiladores/T2/Compiladores-T2/src/main/antlr4
[INFO] Processing grammar: br/ufscar/dc/compiladores/NossaGramatica.g4
[INFO] 
[INFO] --- resources:3.3.1:resources (default-resources) @ NossaLexica ---
[INFO] skip non existing resourceDirectory /Users/danilosp/Desktop/Folder/Faculdade/Compiladores/T2/Compiladores-T2/src/main/resources
[INFO] 
[INFO] --- compiler:3.13.0:compile (default-compile) @ NossaLexica ---
[INFO] Recompiling the module because of changed source code.
[INFO] Compiling 5 source files with javac [debug target 17] to target/classes
[WARNING] system modules path not set in conjunction with -source 17
[INFO] 
[INFO] --- resources:3.3.1:testResources (default-testResources) @ NossaLexica ---
[INFO] skip non existing resourceDirectory /Users/danilosp/Desktop/Folder/Faculdade/Compiladores/T2/Compiladores-T2/src/test/resources
[INFO] 
[INFO] --- compiler:3.13.0:testCompile (default-testCompile) @ NossaLexica ---
[INFO] No sources to compile
[INFO] 
[INFO] --- surefire:3.2.5:test (default-test) @ NossaLexica ---
[INFO] No tests to run.
[INFO] 
[INFO] --- jar:3.4.1:jar (default-jar) @ NossaLexica ---
[INFO] Building jar: /Users/danilosp/Desktop/Folder/Faculdade/Compiladores/T2/Compiladores-T2/target/NossaLexica-1.0-SNAPSHOT.jar
[INFO] 
[INFO] --- shade:3.4.1:shade (default) @ NossaLexica ---
[INFO] Including org.antlr:antlr4:jar:4.13.2 in the shaded jar.
[INFO] Including org.antlr:antlr4-runtime:jar:4.13.2 in the shaded jar.
[INFO] Including org.antlr:antlr-runtime:jar:3.5.3 in the shaded jar.
[INFO] Including org.antlr:ST4:jar:4.3.4 in the shaded jar.
[INFO] Including org.abego.treelayout:org.abego.treelayout.core:jar:1.0.3 in the shaded jar.
[INFO] Including com.ibm.icu:icu4j:jar:72.1 in the shaded jar.
[WARNING] NossaLexica-1.0-SNAPSHOT.jar, ST4-4.3.4.jar, antlr-runtime-3.5.3.jar, antlr4-4.13.2.jar, antlr4-runtime-4.13.2.jar, icu4j-72.1.jar, org.abego.treelayout.core-1.0.3.jar define 1 overlapping resource: 
[WARNING]   - META-INF/MANIFEST.MF
[WARNING] maven-shade-plugin has detected that some class files are
[WARNING] present in two or more JARs. When this happens, only one
[WARNING] single version of the class is copied to the uber jar.
[WARNING] Usually this is not harmful and you can skip these warnings,
[WARNING] otherwise try to manually exclude artifacts based on
[WARNING] mvn dependency:tree -Ddetail=true and the above output.
[WARNING] See https://maven.apache.org/plugins/maven-shade-plugin/
[INFO] Replacing original artifact with shaded artifact.
[INFO] Replacing /Users/danilosp/Desktop/Folder/Faculdade/Compiladores/T2/Compiladores-T2/target/NossaLexica-1.0-SNAPSHOT.jar with /Users/danilosp/Desktop/Folder/Faculdade/Compiladores/T2/Compiladores-T2/target/NossaLexica-1.0-SNAPSHOT-shaded.jar
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  3.635 s
[INFO] Finished at: 2025-04-30T15:09:31-03:00
[INFO] ------------------------------------------------------------------------
```



## Execução do programa:

Após isso, podemos executar a aplicação com o seguinte comando:
```
java -jar target/NossaLexica-1.0-SNAPSHOT.jar src/main/java/br/ufscar/dc/compiladores/entradaJander1.txt src/main/java/br/ufscar/dc/compiladores/saidaJander1.txt
```

Observação: Se estiver encontrando erros na execução após alterar o arquivo NossaGramatica.g4, deve-se utilizar o seguinte comando para limpar a pasta target e começar novamente o passo a passo:
```
mvn clean
```


## Execução dos testes:

```
java -jar compiladores-corretor-automatico-1.0-SNAPSHOT-jar-with-dependencies.jar 'java -jar T2/target/NossaLexica-1.0-SNAPSHOT.jar' gcc ./temp casos-de-teste "811537, 813583, 812124" t2
```

#### Saída após execução dos testes:
- T1 - Não passa em todos os testes pois, ao encontrar um erro, ele sequer chega a imprimir na saída os tokens, já que agora foi implementado o analisador sintático.
```
==================================
Nota do grupo "811537, 813583, 812124":
CT 1 = 7.837838 (29/37)
CT 2 = 0.0 (0/62)
CT 3 = 0.0 (0/9)
CT 4 = 0.0 (0/9)
CT 5 = 0.0 (0/20)
==================================
```

- T2
```
==================================
Nota do grupo "811537, 813583, 812124":
CT 1 = 0.0 (0/37)
CT 2 = 10.0 (62/62)
CT 3 = 0.0 (0/9)
CT 4 = 0.0 (0/9)
CT 5 = 0.0 (0/20)
==================================

```