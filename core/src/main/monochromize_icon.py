from PIL import Image

img = Image.open("ic_launcher-web.png").convert("RGBA")

# Original logo is blue #0093FD -> 0, 147, 253
_, _, b_channel, _ = img.split()

# Map blue 253 to alpha 255
alpha_channel = b_channel.point(lambda pixel_value: int((pixel_value / 253.0) * 255.0))

# Get black-on-transparent image
black_canvas = Image.new("RGBA", img.size, (0, 0, 0, 255))
black_canvas.putalpha(alpha_channel)
black_canvas.save("ic_launcher-monochrome.png", "PNG")
# Get blue-on-transparent image
blue_canvas = Image.new("RGBA", img.size, (0, 147, 253, 255))
blue_canvas.putalpha(alpha_channel)
blue_canvas.save("ic_launcher-transparent.png", "PNG")
