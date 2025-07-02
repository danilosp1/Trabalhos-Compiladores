from moviepy import ImageClip, AudioFileClip, CompositeVideoClip, TextClip, vfx

ronaldinho1_path = "./assets/ronaldinho1.jpg"
ronaldinho2_path = "./assets/ronaldinho2.jpg"
ronaldinho3_path = "./assets/ronaldinho3.jpg"
trilha_animada_path = "./assets/musica_upbeat.mp3"


video = CompositeVideoClip([], size=(1080, 1920))
final_video = video

final_video.write_videofile("video_final.mp4", fps=24)
