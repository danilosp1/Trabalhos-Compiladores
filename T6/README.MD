# Trabalho 6 de Compiladores
Professor Daniel Lucrédio <br>
1 de Julho de 2025

Integrantes:
- Danilo da Silva Pinto, 811537
- Lucas de Araujo Cardoso, 813583
- Pedro Vinícius Guandalini Vicente, 812124

# Linguagem de Geração de Vídeos com Áudios e Imagens

Neste trabalho foi desenvolvido um compilador para uma linguagem capaz de gerar vídeos customizáveis através da junção de Imagens, Áudios, Legendas e Efeitos de transição e edição.

Para que isso seja possível, nosso compilador conta com um sistema de geração de código Python compatível com a biblioteca MoviePy que é capaz de executar de fato os comandos descritos na nossa linguagem simplificada.

Documentação MoviePy: https://zulko.github.io/moviepy
Repositório MoviePy: https://github.com/Zulko/moviepy

## Vídeo de Apresentação
....

## Exemplos de Código
A nossa linguagem simplificada e com recursos no idioma português possui o seguinte funcionamento conforme o exemplo apresentado:

Nossa Linguagem (exemplos/entrada.vid)
```
carregar imagem ronaldinho1 de "./assets/ronaldinho1.jpg"
carregar imagem ronaldinho2 de "./assets/ronaldinho2.png"
carregar imagem ronaldinho3 de "./assets/ronaldinho3.png"
carregar audio trilha_animada de "./assets/musica_upbeat.mp3"

cena {
    duracao: 21s

    criar texto "TOP 10 FOTOS DO BRUXO!!!!" {
        fonte: "Arial Bold"
        tamanho_fonte: 72
        cor: 'preto'
        posicao: (centro, '80%') 
        inicio em 0s por 5s
    }

    usar imagem ronaldinho1 {
        posicao: (centro, centro)
        inicio em 5s por 5s
        efeito: 'fade_in' 1s, 'fade_out' 1s
    }

    usar imagem ronaldinho2 {
        posicao: (centro, centro)
        inicio em 10s por 5s
        efeito: 'blur_in' 1s, 'blur_out' 1s
    }

    usar imagem ronaldinho3 {
        posicao: (centro, centro)
        inicio em 15s por 5s
        efeito: 'zoom_in' 1s, 'zoom_out' 1s
    }

    adicionar audio trilha_animada com volume 80%
}

renderizar para "video_ronaldinho.mp4" com resolucao (1080, 1080)
```

Saída Obtida (exemplos/saidaEsperada.py)
```
from moviepy.editor import *
from moviepy.video.fx.all import fadein, fadeout, blur

ronaldinho1 = ImageClip("./assets/ronaldinho1.jpg")
ronaldinho2 = ImageClip("./assets/ronaldinho2.png")
ronaldinho3 = ImageClip("./assets/ronaldinho3.png")
trilha_animada = AudioFileClip("./assets/musica_upbeat.mp3")

texto0 = TextClip(
    text="TOP 10 FOTOS DO BRUXO!!!!",
    fontsize=72,
    font="Arial-Bold",
    color='black'
).set_position(('centro', '80%')).set_duration(5).set_start(0)

img1 = ronaldinho1.set_position(('centro', 'centro')).set_duration(5).set_start(5)
img1 = fadein(img1, 1)
img1 = fadeout(img1, 1)

img2 = ronaldinho2.set_position(('centro', 'centro')).set_duration(5).set_start(10)
img2 = blur(img2, duration=1)
img2 = blur(img2, duration=1)

img3 = ronaldinho3.set_position(('centro', 'centro')).set_duration(5).set_start(15)
img3 = img3.resize(lambda t: 1 + 0.2 * t / 5)
img3 = img3.resize(lambda t: 1.2 - 0.2 * t / 5)

trilha_animada_final = trilha_animada.volumex(0,80)


clips = [texto0, img1, img2, img3]
video = CompositeVideoClip(clips, size=(1080, 1080)).set_duration(21)

video = video.set_audio(trilha_animada_final)

video.write_videofile("video_ronaldinho.mp4", fps=24)

```
### Verificação de Erros

O sistema também possui mecanismos de verificação de erros léxicos, sintáticos e semânticos, como mostrado no exemplo abaixo:

```
(exemplo de erros captados, e detalhar melhor quais foram os 3/4 principais erros semanticos que são verificados)
```


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
[INFO] Deleting /Users/danilosp/Desktop/Folder/Faculdade/Compiladores/Trabalho-Compiladores/T5/target
[INFO] 
[INFO] --- antlr4:4.13.2:antlr4 (antlr) @ NossaLexica ---
[INFO] ANTLR 4: Processing source directory /Users/danilosp/Desktop/Folder/Faculdade/Compiladores/Trabalho-Compiladores/T5/src/main/antlr4
[INFO] Processing grammar: br/ufscar/dc/compiladores/NossaGramatica.g4
[INFO] 
[INFO] --- resources:3.3.1:resources (default-resources) @ NossaLexica ---
[INFO] skip non existing resourceDirectory /Users/danilosp/Desktop/Folder/Faculdade/Compiladores/Trabalho-Compiladores/T5/src/main/resources
[INFO] 
[INFO] --- compiler:3.13.0:compile (default-compile) @ NossaLexica ---
[INFO] Recompiling the module because of changed source code.
[INFO] Compiling 9 source files with javac [debug target 17] to target/classes
[WARNING] system modules path not set in conjunction with -source 17
[INFO] 
[INFO] --- resources:3.3.1:testResources (default-testResources) @ NossaLexica ---
[INFO] skip non existing resourceDirectory /Users/danilosp/Desktop/Folder/Faculdade/Compiladores/Trabalho-Compiladores/T5/src/test/resources
[INFO] 
[INFO] --- compiler:3.13.0:testCompile (default-testCompile) @ NossaLexica ---
[INFO] No sources to compile
[INFO] 
[INFO] --- surefire:3.2.5:test (default-test) @ NossaLexica ---
[INFO] No tests to run.
[INFO] 
[INFO] --- jar:3.4.1:jar (default-jar) @ NossaLexica ---
[INFO] Building jar: /Users/danilosp/Desktop/Folder/Faculdade/Compiladores/Trabalho-Compiladores/T5/target/NossaLexica-1.0-SNAPSHOT.jar
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
[INFO] Replacing /Users/danilosp/Desktop/Folder/Faculdade/Compiladores/Trabalho-Compiladores/T5/target/NossaLexica-1.0-SNAPSHOT.jar with /Users/danilosp/Desktop/Folder/Faculdade/Compiladores/Trabalho-Compiladores/T5/target/NossaLexica-1.0-SNAPSHOT-shaded.jar
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  7.808 s
[INFO] Finished at: 2025-06-11T20:12:12-03:00
[INFO] ------------------------------------------------------------------------
```

## Execução do programa:

Após isso, podemos executar a aplicação com o seguinte comando:
```
java -jar target/NossaLexica-1.0-SNAPSHOT.jar exemplos/entrada.vid exemplos/saida.py
```

<!-- Observação: Se estiver encontrando erros na execução após alterar o arquivo NossaGramatica.g4, deve-se utilizar o seguinte comando para limpar a pasta target e começar novamente o passo a passo:
```
mvn clean
``` -->

## Geração do vídeo a partir do código gerado

Agora para gerar o vídeo a partir do código gerado pelo nosso compilador, precisamos configurar o ambiente Python: 

(Baixar o Python3...)

Criar as variáveis de ambiente para fazer instalações locais e não globais:
```
python3 -m venv venv
```

Ativar o ambiente virtual
```
source venv/bin/activate
```

Baixar a biblioteca MoviePy juntamente com outras libs necessárias
```
pip install -r requirements.txt
```

Gerar o vídeo de teste com os assets já disponíveis:
```
python3 exemplos/saida.py 
```


## Execução dos testes:

```
(talvez fazer script para rodar varios exemplos de entrada e saida esperados como testes para mostrar que as analises lexicas, sintaticas e semanticas e geracao de codigo funcionam)
```

#### Saída após execução dos testes:
```
(saidas obtidas com esses testes, mostrar que ele captou os erros lexicos/sintaticos/semanticos corretamente onde tinha e gerou o código python corretamente onde tinha) 
```