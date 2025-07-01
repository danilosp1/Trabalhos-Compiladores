from moviepy.editor import *
from moviepy.video.fx.all import fadein, fadeout, blur

# === Recursos carregados ===
ronaldinho1 = ImageClip("./assets/ronaldinho1.jpg")
ronaldinho2 = ImageClip("./assets/ronaldinho2.png")
ronaldinho3 = ImageClip("./assets/ronaldinho3.png")
trilha_animada = AudioFileClip("./assets/musica_upbeat.mp3")

# === Texto 1 ===
texto0 = TextClip(
    text="TOP 10 FOTOS DO BRUXO!!!!",
    fontsize=72,
    font="Arial-Bold",
    color='black'
).set_position(('centro', '80%')).set_duration(5).set_start(0)

# === Imagem 2 (ronaldinho1) ===
img1 = ronaldinho1.set_position(('centro', 'centro')).set_duration(5).set_start(5)
img1 = fadein(img1, 1)
img1 = fadeout(img1, 1)

# === Imagem 3 (ronaldinho2) ===
img2 = ronaldinho2.set_position(('centro', 'centro')).set_duration(5).set_start(10)
img2 = blur(img2, duration=1)
img2 = blur(img2, duration=1)

# === Imagem 4 (ronaldinho3) ===
img3 = ronaldinho3.set_position(('centro', 'centro')).set_duration(5).set_start(15)
img3 = img3.resize(lambda t: 1 + 0.2 * t / 5)
img3 = img3.resize(lambda t: 1.2 - 0.2 * t / 5)

# === Processamento de Áudio ===
trilha_animada_final = trilha_animada.volumex(0,80)


# === Cena completa ===
clips = [texto0, img1, img2, img3]
video = CompositeVideoClip(clips, size=(1080, 1080)).set_duration(21)

# === Adiciona áudio ===
video = video.set_audio(trilha_animada_final)

# === Exporta vídeo ===
video.write_videofile("video_ronaldinho.mp4", fps=24)
