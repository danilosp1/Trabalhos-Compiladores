from moviepy import ImageClip, AudioFileClip, CompositeVideoClip, TextClip, vfx

gato1_path = "./assets/gato1.jpg"
gato2_path = "./assets/gato2.jpg"
gato3_path = "./assets/gato3.jpeg"
gato4_path = "./assets/gato4.jpeg"
trilha_suave_path = "./assets/musica_suave.mp3"

txt_clip0 = TextClip(
    text="Melhores Raças \nde Gatos para \nCasa Pequena!\n",
    font="./assets/ARIAL.TTF",
    font_size=130,
    color='white'
).with_start(0).with_duration(5).with_position('center')

txt_clip1 = TextClip(
    text="Gato Persa!\n",
    font="./assets/ARIAL.TTF",
    font_size=114,
    color='black'
).with_start(5).with_duration(4).with_position('top')

txt_clip2 = TextClip(
    text="Gato de \nPelo Curto!\n",
    font="./assets/ARIAL.TTF",
    font_size=114,
    color='blue'
).with_start(10).with_duration(4).with_position('right')

txt_clip3 = TextClip(
    text="Gato Exotico!\n",
    font="./assets/ARIAL.TTF",
    font_size=114,
    color='yellow'
).with_start(15).with_duration(4).with_position('left')

txt_clip4 = TextClip(
    text="Gato da\n Escócia!\n",
    font="./assets/ARIAL.TTF",
    font_size=114,
    color='brown'
).with_start(20).with_duration(4).with_position('bottom')

txt_clip5 = TextClip(
    text="Excelentes Opções!\n Inscreva-se \nPara Mais \nDicas!!",
    font="./assets/ARIAL.TTF",
    font_size=130,
    color='white'
).with_start(25).with_duration(5).with_position('center')

clip0 = ImageClip(gato1_path)
clip0 = clip0.with_position('center')
clip0 = clip0.resized(width=1080, height=1920)
clip0 = clip0.with_start(5).with_duration(5)
clip0 = clip0.with_effects([vfx.CrossFadeIn(1)])

clip1 = ImageClip(gato2_path)
clip1 = clip1.with_position('center')
clip1 = clip1.resized(width=1080, height=1920)
clip1 = clip1.with_start(10).with_duration(5)
clip1 = clip1.with_effects([vfx.CrossFadeOut(1)])

clip2 = ImageClip(gato3_path)
clip2 = clip2.with_position('center')
clip2 = clip2.resized(width=1080, height=1920)
clip2 = clip2.with_start(15).with_duration(5)
clip2 = clip2.with_effects([vfx.CrossFadeIn(1)])

clip3 = ImageClip(gato4_path)
clip3 = clip3.with_position('center')
clip3 = clip3.resized(width=1080, height=1920)
clip3 = clip3.with_start(20).with_duration(5)
clip3 = clip3.with_effects([vfx.CrossFadeIn(1)])


video = CompositeVideoClip([clip0, clip1, clip2, clip3], size=(1080, 1920))

audio = AudioFileClip(trilha_suave_path)
audio = audio.subclipped(0, 26)
audio = audio.with_volume_scaled(0,80)
video = video.with_audio(audio)

final_video = CompositeVideoClip([video, txt_clip0, txt_clip1, txt_clip2, txt_clip3, txt_clip4, txt_clip5], size=(1080, 1920))

final_video.write_videofile("video_gatos.mp4", fps=24)
