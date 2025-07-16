from moviepy import ImageClip, AudioFileClip, CompositeVideoClip, TextClip, vfx

ronaldinho1_path = "./assets/ronaldinho4.jpg"
ronaldinho2_path = "./assets/ronaldinho5.jpg"
ronaldinho3_path = "./assets/ronaldinho_drible.jpg"
trilha_animada_path = "./assets/musica_upbeat.mp3"

txt_clip0 = TextClip(
    text="MELHORES\n FOTOS \nDO BRUXO!!!!\n",
    font="./assets/ARIAL.TTF",
    font_size=100,
    color='red'
).with_start(0).with_duration(6).with_position('center')

txt_clip1 = TextClip(
    text="Ronaldinho\n no\n Barça!\n",
    font="./assets/ARIAL.TTF",
    font_size=114,
    color='white'
).with_start(6).with_duration(5).with_position('top')

txt_clip2 = TextClip(
    text="Ronaldinho Jovem\n Dribles!\n",
    font="./assets/ARIAL.TTF",
    font_size=114,
    color='yellow'
).with_start(12).with_duration(5).with_position('right')

txt_clip3 = TextClip(
    text="Ronaldão na\n Copa!\n",
    font="./assets/ARIAL.TTF",
    font_size=114,
    color='purple'
).with_start(18).with_duration(5).with_position('left')

clip0 = ImageClip(ronaldinho1_path)
clip0 = clip0.with_position('center')
clip0 = clip0.resized(width=1080, height=1920)
clip0 = clip0.with_start(6).with_duration(6)
clip0 = clip0.with_effects([vfx.Blink(duration_on=2.5, duration_off=0.2)])

clip1 = ImageClip(ronaldinho2_path)
clip1 = clip1.with_position('center')
clip1 = clip1.resized(width=1080, height=1920)
clip1 = clip1.with_start(12).with_duration(6)
clip1 = clip1.with_effects([vfx.Crop(x1=2, width=2)])

clip2 = ImageClip(ronaldinho3_path)
clip2 = clip2.with_position('center')
clip2 = clip2.resized(width=1080, height=1920)
clip2 = clip2.with_start(18).with_duration(6)
clip2 = clip2.with_effects([vfx.Scroll(w=1000, x_speed=200)])

video = CompositeVideoClip([clip0, clip1, clip2], size=(1080, 1920))

audio = AudioFileClip(trilha_animada_path)
audio = audio.subclipped(0, 24)
audio = audio.with_volume_scaled(0,80)
video = video.with_audio(audio)

final_video = CompositeVideoClip([video, txt_clip0, txt_clip1, txt_clip2, txt_clip3], size=(1080, 1920))

final_video.write_videofile("video_ronaldinho.mp4", fps=24)
