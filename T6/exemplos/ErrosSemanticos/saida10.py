from moviepy import ImageClip, AudioFileClip, CompositeVideoClip, TextClip, vfx

ronaldinho1_path = "./assets/ronaldinho1.jpg"
ronaldinho2_path = "./assets/ronaldinho2.jpg"
ronaldinho3_path = "./assets/ronaldinho3.jpg"
trilha_animada_path = "./assets/musica_upbeat.mp3"

txt_clip0 = TextClip(
    text="TOP 10 FOTOS DO BRUXO!!!!",
    font="./assets/ARIAL.TTF",
    font_size=72,
    color='white'
).with_start(0).with_duration(25).with_position('center')

clip0 = ImageClip(ronaldinho1_path)
clip0 = clip0.with_position('center')
clip0 = clip0.resized(width=1080, height=1920)
clip0 = clip0.with_start(5).with_duration(5)
clip0 = clip0.with_effects([vfx.CrossFadeIn(1)])

clip1 = ImageClip(ronaldinho2_path)
clip1 = clip1.with_position('center')
clip1 = clip1.resized(width=1080, height=1920)
clip1 = clip1.with_start(10).with_duration(5)
clip1 = clip1.with_effects([vfx.CrossFadeOut(1)])

clip2 = ImageClip(ronaldinho3_path)
clip2 = clip2.with_position('center')
clip2 = clip2.resized(width=1080, height=1920)
clip2 = clip2.with_start(15).with_duration(5)
clip2 = clip2.with_effects([vfx.CrossFadeIn(1)])


video = CompositeVideoClip([clip0, clip1, clip2], size=(1080, 1920))

audio = AudioFileClip(trilha_animada_path)
audio = audio.subclipped(0, 21)
audio = audio.with_volume_scaled(0,80)
video = video.with_audio(audio)

final_video = CompositeVideoClip([video, txt_clip0], size=(1080, 1920))

final_video.write_videofile("video_ronaldinho.mp4", fps=24)
