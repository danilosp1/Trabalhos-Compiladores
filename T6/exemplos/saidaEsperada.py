from moviepy.editor import *
from moviepy.video.fx.all import fadein, fadeout, blur

# === Recursos carregados ===
ronaldinho1 = ImageClip("./assets/ronaldinho1.jpg")
ronaldinho2 = ImageClip("./assets/ronaldinho2.png")
ronaldinho3 = ImageClip("./assets/ronaldinho3.png")
trilha_animada = AudioFileClip("./assets/musica_upbeat.mp3")

# === Texto inicial ===
texto = TextClip(
    "TOP 10 FOTOS DO BRUXO!!!!",
    fontsize=72,
    font="Arial-Bold",
    color='black'
).set_position(("center", "80%")).set_duration(5).set_start(0)

# === Imagem 1 com efeito fade ===
img1 = ronaldinho1.set_position("center").set_duration(5).set_start(5)
img1 = fadein(img1, 1)
img1 = fadeout(img1, 1)

# === Imagem 2 com efeito blur ===
img2 = ronaldinho2.set_position("center").set_duration(5).set_start(10)
img2 = blur(img2, 1)  # blur_in
img2 = blur(img2, 1)  # blur_out

# === Imagem 3 com efeito zoom ===
img3 = ronaldinho3.set_position("center").set_duration(5).set_start(15)
img3 = img3.resize(lambda t: 1 + 0.1 * t)  # zoom_in
img3 = img3.resize(lambda t: 1.5 - 0.1 * t)  # zoom_out

# === Cena completa ===
clips = [texto, img1, img2, img3]
video = CompositeVideoClip(clips, size=(1080, 1080)).set_duration(21)

# === Adiciona áudio ===
video = video.set_audio(trilha_animada.volumex(0.8))

# === Exporta vídeo ===
video.write_videofile("video_ronaldinho.mp4", fps=24)
