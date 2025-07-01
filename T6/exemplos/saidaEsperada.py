from moviepy import ImageClip, AudioFileClip, CompositeVideoClip, TextClip, vfx

imagem1 = "assets/ronaldinho1.jpg"
imagem2 = "assets/ronaldinho2.jpg"
imagem3 = "assets/ronaldinho3.jpg"

total_duration = 21

txt_clip = TextClip(
    text="TOP 10 FOTOS DO BRUXO!!!!",
    font="assets/ARIAL.TTF",
    font_size=100,
    color='white'
).with_start(0).with_duration(5).with_position('center')

clip1 = ImageClip(imagem1)
clip1 = clip1.with_position('center')
clip1 = clip1.resized(width=1080, height=1920)
clip1 = clip1.with_start(5).with_duration(5)
clip1 = clip1.with_effects([vfx.CrossFadeIn(1)])

clip2 = ImageClip(imagem2)
clip2 = clip2.with_position('center')
clip2 = clip2.resized(width=1080, height=1920)
clip2 = clip2.with_start(10).with_duration(5)
clip2 = clip2.with_effects([vfx.CrossFadeOut(1)])

clip3 = ImageClip(imagem3)
clip3 = clip3.with_position('center')
clip3 = clip3.resized(width=1080, height=1920)
clip3 = clip3.with_start(15).with_duration(5)
clip3 = clip3.with_effects([vfx.CrossFadeIn(1)])

video = CompositeVideoClip([clip1, clip2, clip3], size=(1080, 1920))
video = video.with_duration(total_duration)

audio = AudioFileClip("assets/musica_upbeat.mp3")
audio = audio.subclipped(0, 21)

video = video.with_audio(audio)
final_video = CompositeVideoClip([video, txt_clip], size=(1080, 1920))

final_video.write_videofile("video_ronaldinho.mp4", fps=24)