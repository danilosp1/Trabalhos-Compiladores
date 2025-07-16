from moviepy import ImageClip, AudioFileClip, CompositeVideoClip, TextClip, vfx

prof1_path = "./assets/prof1.jpg"
prof2_path = "./assets/prof2.jpeg"
prof3_path = "./assets/prof3.png"
trilha_feliz_path = "./assets/musica_feliz.mp3"

txt_clip0 = TextClip(
    text="TOP 3\n Professores Mais\n​ CHARMOSOS\n da UFSCar!!\n (˵ •̀ ᴗ - ˵ )\n",
    font="./assets/ARIAL.TTF",
    font_size=130,
    color='white'
).with_start(0).with_duration(7).with_position('center')

txt_clip1 = TextClip(
    text="JANDER!!! (..◜ᴗ◝..) ​\n",
    font="./assets/ARIAL.TTF",
    font_size=114,
    color='black'
).with_start(7).with_duration(5).with_position('top')

txt_clip2 = TextClip(
    text="Fabi!​​\n (˶ˆᗜˆ˵)\n",
    font="./assets/ARIAL.TTF",
    font_size=114,
    color='blue'
).with_start(14).with_duration(5).with_position('right')

txt_clip3 = TextClip(
    text="Lucredio!!\n <3 <3\n",
    font="./assets/ARIAL.TTF",
    font_size=114,
    color='red'
).with_start(20).with_duration(5).with_position('top')

clip0 = ImageClip(prof2_path)
clip0 = clip0.with_position('center')
clip0 = clip0.resized(width=1080, height=1920)
clip0 = clip0.with_start(7).with_duration(6)
clip0 = clip0.with_effects([vfx.CrossFadeIn(1)])

clip1 = ImageClip(prof3_path)
clip1 = clip1.with_position('center')
clip1 = clip1.resized(width=1080, height=1920)
clip1 = clip1.with_start(14).with_duration(6)
clip1 = clip1.with_effects([vfx.CrossFadeOut(1)])

clip2 = ImageClip(prof1_path)
clip2 = clip2.with_position('center')
clip2 = clip2.resized(width=1080, height=1920)
clip2 = clip2.with_start(20).with_duration(6)
clip2 = clip2.with_effects([vfx.Scroll(w=1000, x_speed=200)])


video = CompositeVideoClip([clip0, clip1, clip2], size=(1080, 1920))

audio = AudioFileClip(trilha_feliz_path)
audio = audio.subclipped(0, 26)
audio = audio.with_volume_scaled(0,80)
video = video.with_audio(audio)

final_video = CompositeVideoClip([video, txt_clip0, txt_clip1, txt_clip2, txt_clip3], size=(1080, 1920))

final_video.write_videofile("video_profs.mp4", fps=24)
