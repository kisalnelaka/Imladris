#!/usr/bin/env python3
"""
IMLADRIS Launch Video Generator
Renders a 1080p 60fps cinematic launch video with ethereal LOTR Elven aesthetics.
Synthesizes a majestic Elven harp & ambient celestial soundtrack using NumPy.
"""

import sys
import os
import math
import wave
import struct
import subprocess
import cairo
import numpy as np

# Video configuration
WIDTH = 1920
HEIGHT = 1080
FPS = 60
DURATION = 45.0  # seconds
TOTAL_FRAMES = int(DURATION * FPS)
OUTPUT_VIDEO = "/home/kisalnelaka/Work/Imladris/assets/imladris_launch.mp4"
AUDIO_WAV = "/tmp/imladris_soundtrack.wav"

# Color Palette (Rivendell / Ethereal Elven)
C_MIDNIGHT_DEEP = (0.02, 0.03, 0.06)
C_MIDNIGHT_BLUE = (0.05, 0.08, 0.14)
C_MIST_BLUE     = (0.09, 0.14, 0.22)
C_SILVER        = (0.88, 0.92, 0.96)
C_SILVER_MUTED  = (0.60, 0.68, 0.76)
C_GOLD          = (0.86, 0.72, 0.35)
C_GOLD_LIGHT    = (0.97, 0.90, 0.65)
C_MITHRIL_TEAL  = (0.42, 0.82, 0.88)
C_EMERALD_HINT  = (0.25, 0.78, 0.58)

# ---------------------------------------------------------------------------
# AUDIO SYNTHESIS: Majestic Elven Harp & Ambient Drone Soundtrack
# ---------------------------------------------------------------------------
def synthesize_elven_soundtrack(output_path, duration=45.0, sample_rate=44100):
    print(f"[1/3] Synthesizing {duration}s Elven Harp & Ambient Soundtrack...")
    total_samples = int(duration * sample_rate)
    t = np.linspace(0, duration, total_samples, endpoint=False)
    
    # Left and Right stereo channels
    audio_l = np.zeros(total_samples, dtype=np.float32)
    audio_r = np.zeros(total_samples, dtype=np.float32)

    # 1. Deep Celestial Drone (Eolian Sanctuary Root E)
    # E2 = 82.41 Hz, B2 = 123.47 Hz, E3 = 164.81 Hz, G3 = 196.00 Hz
    drone_freqs = [82.41, 123.47, 164.81, 196.00, 246.94]
    drone_weights = [0.22, 0.15, 0.10, 0.08, 0.05]
    
    for f, w in zip(drone_freqs, drone_weights):
        # Subtle slow LFO detune
        lfo_l = 1.0 + 0.003 * np.sin(2 * np.pi * 0.11 * t)
        lfo_r = 1.0 + 0.003 * np.cos(2 * np.pi * 0.14 * t)
        drone_l = np.sin(2 * np.pi * (f * lfo_l) * t) * w
        drone_r = np.sin(2 * np.pi * (f * lfo_r) * t + 0.5) * w
        audio_l += drone_l
        audio_r += drone_r

    # 2. Ambient Ethereal Wind / Mist (Filtered pinkish noise)
    noise = np.random.normal(0, 0.015, total_samples).astype(np.float32)
    # Soft moving filter envelope
    wind_env = 0.5 + 0.5 * np.sin(2 * np.pi * 0.08 * t)
    audio_l += noise * wind_env
    audio_r += noise * (1.0 - wind_env * 0.5)

    # 3. Elven Harp Arpeggio Synthesizer
    # Harp pluck model: Fundamental + decaying harmonics with gentle touch
    def pluck(freq, start_sec, decay=2.5, pan=0.0, volume=0.22):
        start_idx = int(start_sec * sample_rate)
        note_len = int(decay * sample_rate)
        if start_idx >= total_samples:
            return
        end_idx = min(start_idx + note_len, total_samples)
        actual_len = end_idx - start_idx
        
        t_note = np.linspace(0, actual_len / sample_rate, actual_len, endpoint=False)
        # Envelope: immediate soft attack (5ms), exponential decay
        attack_len = int(0.005 * sample_rate)
        env = np.exp(-t_note * (3.8 / decay))
        if actual_len > attack_len:
            env[:attack_len] *= np.linspace(0, 1, attack_len)
        
        # Additive harp harmonics (warm, resonant bell-string timbre)
        signal = (
            np.sin(2 * np.pi * freq * t_note) * 1.0 +
            np.sin(2 * np.pi * 2 * freq * t_note) * 0.45 +
            np.sin(2 * np.pi * 3 * freq * t_note) * 0.22 +
            np.sin(2 * np.pi * 4 * freq * t_note) * 0.08
        ) * env * volume
        
        # Stereo panning (-1.0 left, +1.0 right)
        pan_l = math.cos((pan + 1.0) * math.pi / 4.0)
        pan_r = math.sin((pan + 1.0) * math.pi / 4.0)
        
        audio_l[start_idx:end_idx] += signal * pan_l
        audio_r[start_idx:end_idx] += signal * pan_r

    # Harmonic scale: E Minor Pentatonic & Dorian (E3 to E6)
    # E3=164.81, G3=196.00, A3=220.00, B3=246.94, D4=293.66, E4=329.63,
    # F#4=369.99, G4=392.00, A4=440.00, B4=493.88, D5=587.33, E5=659.25, G5=783.99, B5=987.77
    scale_notes = [
        164.81, 196.00, 220.00, 246.94, 293.66, 329.63, 369.99,
        392.00, 440.00, 493.88, 587.33, 659.25, 783.99, 987.77
    ]
    
    # Harp sequence pattern throughout 45 seconds
    # Stately, calm, contemplative elven arpeggios
    harp_pattern = [
        # Intro: Awakening of the Sanctuary (0 - 8s)
        (0.8, 329.63, -0.4), (1.4, 392.00, 0.3), (2.1, 493.88, -0.2), (2.8, 659.25, 0.4),
        (4.0, 587.33, 0.1), (4.7, 493.88, -0.3), (5.5, 392.00, 0.2), (6.3, 329.63, 0.0),
        # Scene 2: The Hall of Imladris (8 - 17s)
        (8.2, 246.94, -0.5), (9.0, 329.63, 0.2), (9.8, 440.00, -0.3), (10.6, 493.88, 0.5),
        (11.4, 587.33, -0.2), (12.2, 659.25, 0.3), (13.0, 783.99, -0.4), (14.0, 987.77, 0.4),
        (14.8, 659.25, -0.1), (15.6, 493.88, 0.3), (16.4, 392.00, -0.3),
        # Scene 3: The Constellation Graph (17 - 27s) - Cascading stellar arpeggios
        (17.2, 329.63, -0.6), (17.7, 392.00, -0.3), (18.2, 493.88, 0.1), (18.7, 587.33, 0.4),
        (19.2, 659.25, 0.6), (20.0, 783.99, -0.4), (20.8, 587.33, 0.2), (21.6, 493.88, -0.2),
        (22.4, 392.00, 0.3), (23.2, 440.00, -0.5), (24.0, 493.88, 0.2), (24.8, 659.25, -0.3),
        (25.6, 783.99, 0.5), (26.4, 987.77, 0.0),
        # Scene 4: The Ethereal Reader (27 - 36s) - Quiet, contemplative depth
        (27.4, 164.81, -0.3), (28.4, 246.94, 0.3), (29.4, 329.63, -0.2), (30.4, 392.00, 0.2),
        (31.4, 493.88, -0.4), (32.4, 440.00, 0.4), (33.4, 392.00, -0.2), (34.4, 329.63, 0.1),
        (35.2, 246.94, -0.3),
        # Scene 5: Finale & Sovereign Calling (36 - 44s)
        (36.2, 329.63, -0.5), (36.8, 440.00, 0.4), (37.4, 493.88, -0.2), (38.0, 659.25, 0.3),
        (39.0, 783.99, -0.3), (39.8, 987.77, 0.4), (40.8, 659.25, 0.0), (42.0, 329.63, 0.0)
    ]

    for time_sec, note_freq, pan_val in harp_pattern:
        pluck(note_freq, time_sec, decay=3.2, pan=pan_val, volume=0.24)

    # 4. Crystalline Chimes (Starlight Glisten on scene transitions)
    def chime(freq, start_sec, decay=4.0, volume=0.18):
        start_idx = int(start_sec * sample_rate)
        note_len = int(decay * sample_rate)
        if start_idx >= total_samples:
            return
        end_idx = min(start_idx + note_len, total_samples)
        actual_len = end_idx - start_idx
        t_chime = np.linspace(0, actual_len / sample_rate, actual_len, endpoint=False)
        env = np.exp(-t_chime * (2.5 / decay))
        sig = (np.sin(2 * np.pi * freq * t_chime) + 0.3 * np.sin(2 * np.pi * 2.76 * freq * t_chime)) * env * volume
        audio_l[start_idx:end_idx] += sig * 0.7
        audio_r[start_idx:end_idx] += sig * 0.7

    # Chime triggers
    for c_time in [0.5, 7.8, 16.8, 26.8, 35.8]:
        chime(1318.51, c_time, decay=4.5)  # E6
        chime(1975.53, c_time + 0.15, decay=4.0) # B6

    # 5. Master compression, limiter & smooth fade out
    # Normalize peak to 0.88
    fade_out_samples = int(2.5 * sample_rate)
    fade_curve = np.linspace(1.0, 0.0, fade_out_samples)
    audio_l[-fade_out_samples:] *= fade_curve
    audio_r[-fade_out_samples:] *= fade_curve

    max_peak = max(np.max(np.abs(audio_l)), np.max(np.abs(audio_r)), 0.001)
    norm_factor = 0.88 / max_peak
    audio_l *= norm_factor
    audio_r *= norm_factor

    # Convert to 16-bit PCM stereo
    pcm_l = (audio_l * 32767).astype(np.int16)
    pcm_r = (audio_r * 32767).astype(np.int16)
    interleaved = np.empty((total_samples * 2,), dtype=np.int16)
    interleaved[0::2] = pcm_l
    interleaved[1::2] = pcm_r

    with wave.open(output_path, 'wb') as wf:
        wf.setnchannels(2)
        wf.setsampwidth(2)
        wf.setframerate(sample_rate)
        wf.writeframes(interleaved.tobytes())

    print(f"Soundtrack generated: {output_path} ({os.path.getsize(output_path)} bytes)")


# ---------------------------------------------------------------------------
# VECTOR GRAPHICS & SCENE RENDERING (CAIRO)
# ---------------------------------------------------------------------------

class ParticleSystem:
    def __init__(self, count=90):
        np.random.seed(42)
        self.x = np.random.uniform(50, WIDTH - 50, count)
        self.y = np.random.uniform(50, HEIGHT - 50, count)
        self.speed_y = np.random.uniform(12, 28, count)
        self.speed_x = np.random.uniform(-8, 8, count)
        self.phase = np.random.uniform(0, 2 * math.pi, count)
        self.radius = np.random.uniform(1.2, 3.2, count)
        self.alpha_base = np.random.uniform(0.25, 0.85, count)

    def update(self, dt):
        self.y -= self.speed_y * dt
        self.x += self.speed_x * dt
        self.phase += 1.5 * dt
        # Wrap around edges
        for i in range(len(self.y)):
            if self.y[i] < -20:
                self.y[i] = HEIGHT + 20
                self.x[i] = np.random.uniform(50, WIDTH - 50)
            if self.x[i] < -20:
                self.x[i] = WIDTH + 20
            elif self.x[i] > WIDTH + 20:
                self.x[i] = -20

    def draw(self, ctx):
        for i in range(len(self.x)):
            shimmer = 0.5 + 0.5 * math.sin(self.phase[i])
            a = self.alpha_base[i] * shimmer
            ctx.save()
            # Golden/starlight mote
            ctx.set_source_rgba(C_GOLD_LIGHT[0], C_GOLD_LIGHT[1], C_GOLD_LIGHT[2], a)
            ctx.arc(self.x[i], self.y[i], self.radius[i], 0, 2 * math.pi)
            ctx.fill()
            
            # Subtle outer glow on larger particles
            if self.radius[i] > 2.2:
                ctx.set_source_rgba(C_MITHRIL_TEAL[0], C_MITHRIL_TEAL[1], C_MITHRIL_TEAL[2], a * 0.3)
                ctx.arc(self.x[i], self.y[i], self.radius[i] * 2.5, 0, 2 * math.pi)
                ctx.fill()
            ctx.restore()


# Pure Kotlin-style Physics Knowledge Graph for Scene 3
class PhysicsGraph:
    def __init__(self):
        # 7 canonical knowledge nodes
        self.nodes = [
            {"name": "Elder Lore", "x": 960.0, "y": 540.0, "vx": 0.0, "vy": 0.0, "r": 42.0, "prog": 0.88, "pinned": True},
            {"name": "Sindarin Philology", "x": 780.0, "y": 420.0, "vx": 0.0, "vy": 0.0, "r": 28.0, "prog": 0.65, "pinned": False},
            {"name": "Silmaril Lore", "x": 1140.0, "y": 410.0, "vx": 0.0, "vy": 0.0, "r": 34.0, "prog": 0.94, "pinned": False},
            {"name": "Mind & Memory", "x": 760.0, "y": 680.0, "vx": 0.0, "vy": 0.0, "r": 26.0, "prog": 0.42, "pinned": False},
            {"name": "Celestial Maps", "x": 1160.0, "y": 670.0, "vx": 0.0, "vy": 0.0, "r": 30.0, "prog": 0.72, "pinned": False},
            {"name": "Hymns of Elbereth", "x": 960.0, "y": 320.0, "vx": 0.0, "vy": 0.0, "r": 24.0, "prog": 0.50, "pinned": False},
            {"name": "Haven Cartography", "x": 960.0, "y": 760.0, "vx": 0.0, "vy": 0.0, "r": 25.0, "prog": 0.35, "pinned": False},
        ]
        self.edges = [
            (0, 1), (0, 2), (0, 3), (0, 4), (0, 5), (0, 6),
            (1, 5), (2, 5), (3, 6), (4, 6), (1, 3), (2, 4)
        ]

    def step(self, dt=0.016):
        # Coulomb Repulsion & Hooke Springs
        k_repulse = 18000.0
        k_spring = 0.045
        target_len = 190.0
        damping = 0.85
        
        n = len(self.nodes)
        # Repulsion
        for i in range(n):
            if self.nodes[i]["pinned"]:
                continue
            fx, fy = 0.0, 0.0
            for j in range(n):
                if i == j:
                    continue
                dx = self.nodes[i]["x"] - self.nodes[j]["x"]
                dy = self.nodes[i]["y"] - self.nodes[j]["y"]
                dist_sq = dx * dx + dy * dy + 100.0
                dist = math.sqrt(dist_sq)
                force = k_repulse / dist_sq
                fx += (dx / dist) * force
                fy += (dy / dist) * force
            
            # Center gravity
            fx -= (self.nodes[i]["x"] - 960.0) * 0.01
            fy -= (self.nodes[i]["y"] - 540.0) * 0.01
            
            self.nodes[i]["vx"] = (self.nodes[i]["vx"] + fx * dt) * damping
            self.nodes[i]["vy"] = (self.nodes[i]["vy"] + fy * dt) * damping

        # Spring tension
        for u, v in self.edges:
            dx = self.nodes[v]["x"] - self.nodes[u]["x"]
            dy = self.nodes[v]["y"] - self.nodes[u]["y"]
            dist = math.sqrt(dx * dx + dy * dy) + 0.001
            displacement = dist - target_len
            spring_force = displacement * k_spring
            
            if not self.nodes[u]["pinned"]:
                self.nodes[u]["vx"] += (dx / dist) * spring_force * dt
                self.nodes[u]["vy"] += (dy / dist) * spring_force * dt
            if not self.nodes[v]["pinned"]:
                self.nodes[v]["vx"] -= (dx / dist) * spring_force * dt
                self.nodes[v]["vy"] -= (dy / dist) * spring_force * dt

        # Apply displacement
        for node in self.nodes:
            if not node["pinned"]:
                node["x"] += node["vx"]
                node["y"] += node["vy"]


def draw_elven_star(ctx, cx, cy, radius, rotation=0.0, alpha=1.0):
    """Draws the sacred 8-pointed Star of Eärendil with radiant rays."""
    ctx.save()
    ctx.translate(cx, cy)
    ctx.rotate(rotation)
    
    # 8-pointed star geometry
    points = []
    num_points = 8
    r_outer = radius
    r_inner = radius * 0.38
    for i in range(num_points * 2):
        r = r_outer if i % 2 == 0 else r_inner
        angle = i * math.pi / num_points
        points.append((r * math.sin(angle), -r * math.cos(angle)))
    
    ctx.set_source_rgba(C_GOLD[0], C_GOLD[1], C_GOLD[2], 0.25 * alpha)
    ctx.new_path()
    ctx.move_to(points[0][0], points[0][1])
    for px, py in points[1:]:
        ctx.line_to(px, py)
    ctx.close_path()
    ctx.fill_preserve()
    
    ctx.set_source_rgba(C_GOLD_LIGHT[0], C_GOLD_LIGHT[1], C_GOLD_LIGHT[2], 0.95 * alpha)
    ctx.set_line_width(1.8)
    ctx.stroke()
    
    # Inner halo ring
    ctx.set_source_rgba(C_MITHRIL_TEAL[0], C_MITHRIL_TEAL[1], C_MITHRIL_TEAL[2], 0.6 * alpha)
    ctx.arc(0, 0, radius * 0.45, 0, 2 * math.pi)
    ctx.set_line_width(1.2)
    ctx.stroke()
    
    # Core starpoint
    ctx.set_source_rgba(1.0, 1.0, 1.0, 0.95 * alpha)
    ctx.arc(0, 0, 3.0, 0, 2 * math.pi)
    ctx.fill()
    
    ctx.restore()


def draw_ethereal_frame(ctx, alpha=1.0):
    """Draws an intricate, refined Elven arch filigree border."""
    ctx.save()
    pad = 48
    w = WIDTH - 2 * pad
    h = HEIGHT - 2 * pad
    
    # Delicate dual border lines
    ctx.set_source_rgba(C_GOLD[0], C_GOLD[1], C_GOLD[2], 0.35 * alpha)
    ctx.set_line_width(1.0)
    ctx.rectangle(pad, pad, w, h)
    ctx.stroke()
    
    ctx.set_source_rgba(C_SILVER[0], C_SILVER[1], C_SILVER[2], 0.18 * alpha)
    ctx.set_line_width(0.8)
    ctx.rectangle(pad + 10, pad + 10, w - 20, h - 20)
    ctx.stroke()
    
    # 4 Ornate corner flourishes
    corners = [
        (pad + 10, pad + 10, 0),
        (pad + w - 10, pad + 10, math.pi / 2),
        (pad + w - 10, pad + h - 10, math.pi),
        (pad + 10, pad + h - 10, 3 * math.pi / 2)
    ]
    for cx, cy, rot in corners:
        ctx.save()
        ctx.translate(cx, cy)
        ctx.rotate(rot)
        ctx.set_source_rgba(C_GOLD[0], C_GOLD[1], C_GOLD[2], 0.65 * alpha)
        ctx.set_line_width(1.5)
        # Curved elven leaf flourish
        ctx.arc(24, 24, 24, math.pi, 1.5 * math.pi)
        ctx.stroke()
        # Accent diamond
        ctx.new_path()
        ctx.move_to(0, 0)
        ctx.line_to(6, 6)
        ctx.line_to(12, 0)
        ctx.line_to(6, -6)
        ctx.close_path()
        ctx.fill()
        ctx.restore()
        
    ctx.restore()


def draw_glass_card(ctx, x, y, w, h, radius=18, alpha=1.0):
    """Draws a translucent glassmorphic panel with frosted border."""
    ctx.save()
    # Rounded rectangle path
    degrees = math.pi / 180.0
    ctx.new_path()
    ctx.arc(x + w - radius, y + radius, radius, -90 * degrees, 0 * degrees)
    ctx.arc(x + w - radius, y + h - radius, radius, 0 * degrees, 90 * degrees)
    ctx.arc(x + radius, y + h - radius, radius, 90 * degrees, 180 * degrees)
    ctx.arc(x + radius, y + radius, radius, 180 * degrees, 270 * degrees)
    ctx.close_path()
    
    # Translucent glass fill
    pat = cairo.LinearGradient(x, y, x, y + h)
    pat.add_color_stop_rgba(0.0, 0.12, 0.18, 0.28, 0.55 * alpha)
    pat.add_color_stop_rgba(1.0, 0.05, 0.08, 0.14, 0.70 * alpha)
    ctx.set_source(pat)
    ctx.fill_preserve()
    
    # Frost luminous border
    b_pat = cairo.LinearGradient(x, y, x + w, y + h)
    b_pat.add_color_stop_rgba(0.0, C_MITHRIL_TEAL[0], C_MITHRIL_TEAL[1], C_MITHRIL_TEAL[2], 0.45 * alpha)
    b_pat.add_color_stop_rgba(0.5, C_SILVER[0], C_SILVER[1], C_SILVER[2], 0.20 * alpha)
    b_pat.add_color_stop_rgba(1.0, C_GOLD[0], C_GOLD[1], C_GOLD[2], 0.35 * alpha)
    ctx.set_source(b_pat)
    ctx.set_line_width(1.4)
    ctx.stroke()
    ctx.restore()


# ---------------------------------------------------------------------------
# MAIN RENDERING LOOP (PIPED DIRECTLY TO FFMPEG)
# ---------------------------------------------------------------------------
def render_launch_video():
    synthesize_elven_soundtrack(AUDIO_WAV, duration=DURATION)
    
    print(f"[2/3] Initializing Cairo context and FFmpeg pipe ({WIDTH}x{HEIGHT} @ {FPS}fps, {TOTAL_FRAMES} frames)...")
    
    # Pre-load app icon if available
    app_icon_surface = None
    icon_path = "/home/kisalnelaka/Work/Imladris/docs/assets/app_icon.png"
    if os.path.exists(icon_path):
        try:
            app_icon_surface = cairo.ImageSurface.create_from_png(icon_path)
        except Exception as e:
            print(f"Warning: could not load app icon: {e}")

    # Launch FFmpeg subprocess
    ffmpeg_cmd = [
        "ffmpeg", "-y",
        "-f", "rawvideo",
        "-pix_fmt", "bgra",
        "-s", f"{WIDTH}x{HEIGHT}",
        "-r", str(FPS),
        "-i", "-",
        "-i", AUDIO_WAV,
        "-c:v", "libx264",
        "-preset", "veryfast",
        "-crf", "18",
        "-pix_fmt", "yuv420p",
        "-c:a", "aac",
        "-b:a", "256k",
        "-shortest",
        OUTPUT_VIDEO
    ]
    
    ffmpeg_log = open("/tmp/ffmpeg_render.log", "w")
    proc = subprocess.Popen(ffmpeg_cmd, stdin=subprocess.PIPE, stdout=subprocess.DEVNULL, stderr=ffmpeg_log)
    
    # Cairo Surface
    surface = cairo.ImageSurface(cairo.FORMAT_ARGB32, WIDTH, HEIGHT)
    ctx = cairo.Context(surface)
    
    # Systems
    particles = ParticleSystem(count=100)
    graph = PhysicsGraph()

    print("[3/3] Rendering frames with ethereal Elven visual design...")
    start_time = os.times().user
    
    for frame_idx in range(TOTAL_FRAMES):
        t = frame_idx / float(FPS)
        dt = 1.0 / float(FPS)
        particles.update(dt)
        if 17.0 <= t <= 27.5:
            graph.step(dt)

        # Clear background with Deep Twilight Mist gradient
        ctx.save()
        bg_rad = cairo.RadialGradient(960, 540, 100, 960, 540, 1150)
        # Ethereal midnight aura
        bg_rad.add_color_stop_rgba(0.0, 0.07, 0.12, 0.20, 1.0)
        bg_rad.add_color_stop_rgba(0.55, 0.04, 0.06, 0.11, 1.0)
        bg_rad.add_color_stop_rgba(1.0, 0.015, 0.025, 0.045, 1.0)
        ctx.set_source(bg_rad)
        ctx.paint()
        ctx.restore()

        # Shimmering stardust motes (ambient in all scenes)
        particles.draw(ctx)
        
        # Outer Elven filigree frame
        frame_alpha = min(1.0, t / 1.5) if t < 43.0 else max(0.0, (45.0 - t) / 2.0)
        draw_ethereal_frame(ctx, alpha=frame_alpha)

        # -------------------------------------------------------------------
        # SCENE 1: THE HAVEN AWAKENS (0.0s - 8.0s)
        # -------------------------------------------------------------------
        if t < 8.2:
            sc_alpha = min(1.0, t / 1.8) if t < 6.8 else max(0.0, (8.2 - t) / 1.4)
            ctx.save()
            
            # Central Star of Eärendil & App Icon Aura
            star_rot = t * 0.12
            draw_elven_star(ctx, 960, 370, radius=90.0, rotation=star_rot, alpha=sc_alpha)
            
            # Draw App Icon with soft floating motion
            if app_icon_surface:
                ctx.save()
                icon_w = app_icon_surface.get_width()
                icon_h = app_icon_surface.get_height()
                float_y = 370 + math.sin(t * 1.6) * 8.0
                ctx.translate(960, float_y)
                ctx.scale(0.38, 0.38)
                ctx.set_source_surface(app_icon_surface, -icon_w / 2.0, -icon_h / 2.0)
                ctx.paint_with_alpha(sc_alpha * 0.95)
                ctx.restore()
                
            # Luminous Title: IMLADRIS
            ctx.select_font_face("Liberation Serif", cairo.FONT_SLANT_NORMAL, cairo.FONT_WEIGHT_BOLD)
            ctx.set_font_size(68)
            title = "I M L A D R I S"
            ext = ctx.text_extents(title)
            
            # Title Glow
            ctx.set_source_rgba(C_MITHRIL_TEAL[0], C_MITHRIL_TEAL[1], C_MITHRIL_TEAL[2], 0.35 * sc_alpha)
            ctx.move_to(960 - ext.width / 2.0, 572)
            ctx.show_text(title)
            
            ctx.set_source_rgba(C_SILVER[0], C_SILVER[1], C_SILVER[2], 0.98 * sc_alpha)
            ctx.move_to(960 - ext.width / 2.0, 570)
            ctx.show_text(title)
            
            # Golden Subtitle
            ctx.select_font_face("Liberation Serif", cairo.FONT_SLANT_ITALIC, cairo.FONT_WEIGHT_NORMAL)
            ctx.set_font_size(24)
            sub = "An Ethereal, Offline-First Knowledge Sanctuary"
            sub_ext = ctx.text_extents(sub)
            ctx.set_source_rgba(C_GOLD_LIGHT[0], C_GOLD_LIGHT[1], C_GOLD_LIGHT[2], 0.88 * sc_alpha)
            ctx.move_to(960 - sub_ext.width / 2.0, 625)
            ctx.show_text(sub)
            
            # Sacred Quote
            ctx.set_font_size(20)
            quote = "“The Last Homely House East of the Sea”"
            q_ext = ctx.text_extents(quote)
            ctx.set_source_rgba(C_SILVER_MUTED[0], C_SILVER_MUTED[1], C_SILVER_MUTED[2], 0.75 * sc_alpha)
            ctx.move_to(960 - q_ext.width / 2.0, 690)
            ctx.show_text(quote)

            # Elegant separator flourish
            ctx.set_source_rgba(C_GOLD[0], C_GOLD[1], C_GOLD[2], 0.45 * sc_alpha)
            ctx.set_line_width(1.0)
            ctx.move_to(800, 652)
            ctx.line_to(1120, 652)
            ctx.stroke()
            ctx.arc(960, 652, 3.5, 0, 2 * math.pi)
            ctx.fill()
            
            ctx.restore()

        # -------------------------------------------------------------------
        # SCENE 2: THE HALL & LUMINOUS GATEWAYS (8.0s - 17.0s)
        # -------------------------------------------------------------------
        elif t < 17.2:
            sc_alpha = min(1.0, (t - 8.0) / 1.5) if t < 15.6 else max(0.0, (17.2 - t) / 1.5)
            ctx.save()
            
            # Header
            ctx.select_font_face("Liberation Serif", cairo.FONT_SLANT_NORMAL, cairo.FONT_WEIGHT_BOLD)
            ctx.set_font_size(38)
            hdr = "THE HALL OF IMLADRIS"
            ext = ctx.text_extents(hdr)
            ctx.set_source_rgba(C_SILVER[0], C_SILVER[1], C_SILVER[2], 0.95 * sc_alpha)
            ctx.move_to(960 - ext.width / 2.0, 150)
            ctx.show_text(hdr)
            
            ctx.select_font_face("Liberation Serif", cairo.FONT_SLANT_ITALIC, cairo.FONT_WEIGHT_NORMAL)
            ctx.set_font_size(20)
            sub = "Mae govannen • Spatial Gateways to Realms of Knowledge"
            s_ext = ctx.text_extents(sub)
            ctx.set_source_rgba(C_MITHRIL_TEAL[0], C_MITHRIL_TEAL[1], C_MITHRIL_TEAL[2], 0.85 * sc_alpha)
            ctx.move_to(960 - s_ext.width / 2.0, 190)
            ctx.show_text(sub)
            
            # 3 Luminous Gateway Cards
            gateways = [
                ("REALM OF ELDER LORE", "42 Sacred Scrolls", C_EMERALD_HINT, "Ancient Silvan texts & genealogies"),
                ("PHILOSOPHY OF MIND", "18 Luminous Codices", C_MITHRIL_TEAL, "Cognitive science, phenomenology & thought"),
                ("CELESTIAL CARTOGRAPHY", "29 Star Atlases", C_GOLD_LIGHT, "Mechanics of the heavens & navigation")
            ]
            card_w = 460
            card_h = 320
            start_x = 210
            gap = 60
            
            for i, (g_title, g_count, g_color, g_desc) in enumerate(gateways):
                gx = start_x + i * (card_w + gap)
                gy = 250 + math.sin((t * 2.0) + i * 1.2) * 6.0
                draw_glass_card(ctx, gx, gy, card_w, card_h, radius=16, alpha=sc_alpha)
                
                # Gateway Icon Portal
                draw_elven_star(ctx, gx + 60, gy + 70, radius=24.0, rotation=t * 0.4 + i, alpha=sc_alpha)
                
                # Title
                ctx.select_font_face("Liberation Serif", cairo.FONT_SLANT_NORMAL, cairo.FONT_WEIGHT_BOLD)
                ctx.set_font_size(20)
                ctx.set_source_rgba(C_SILVER[0], C_SILVER[1], C_SILVER[2], 0.95 * sc_alpha)
                ctx.move_to(gx + 105, gy + 68)
                ctx.show_text(g_title)
                
                ctx.select_font_face("Liberation Serif", cairo.FONT_SLANT_NORMAL, cairo.FONT_WEIGHT_NORMAL)
                ctx.set_font_size(15)
                ctx.set_source_rgba(g_color[0], g_color[1], g_color[2], 0.90 * sc_alpha)
                ctx.move_to(gx + 105, gy + 94)
                ctx.show_text(g_count)
                
                # Divider
                ctx.set_source_rgba(C_SILVER_MUTED[0], C_SILVER_MUTED[1], C_SILVER_MUTED[2], 0.25 * sc_alpha)
                ctx.set_line_width(1.0)
                ctx.move_to(gx + 40, gy + 130)
                ctx.line_to(gx + card_w - 40, gy + 130)
                ctx.stroke()
                
                # Description
                ctx.set_font_size(16)
                ctx.set_source_rgba(C_SILVER_MUTED[0], C_SILVER_MUTED[1], C_SILVER_MUTED[2], 0.85 * sc_alpha)
                ctx.move_to(gx + 40, gy + 175)
                ctx.show_text(g_desc)
                
                # Floating progress badge
                ctx.select_font_face("Liberation Serif", cairo.FONT_SLANT_ITALIC, cairo.FONT_WEIGHT_NORMAL)
                ctx.set_font_size(14)
                ctx.set_source_rgba(C_GOLD[0], C_GOLD[1], C_GOLD[2], 0.85 * sc_alpha)
                ctx.move_to(gx + 40, gy + 265)
                ctx.show_text("Portal Open • Touch to enter realm")

            # Floating Recommendation Bar (Current Resonance Engine)
            rec_w = 980
            rec_h = 130
            rec_x = (WIDTH - rec_w) / 2.0
            rec_y = 650 + math.sin(t * 1.5) * 5.0
            draw_glass_card(ctx, rec_x, rec_y, rec_w, rec_h, radius=18, alpha=sc_alpha)
            
            # Rec icon / badge
            draw_elven_star(ctx, rec_x + 55, rec_y + 65, radius=20.0, rotation=-t * 0.3, alpha=sc_alpha)
            
            ctx.select_font_face("Liberation Serif", cairo.FONT_SLANT_NORMAL, cairo.FONT_WEIGHT_BOLD)
            ctx.set_font_size(16)
            ctx.set_source_rgba(C_GOLD[0], C_GOLD[1], C_GOLD[2], 0.95 * sc_alpha)
            ctx.move_to(rec_x + 95, rec_y + 48)
            ctx.show_text("CURRENT RESONANCE • RECOMMENDATION ENGINE")
            
            ctx.select_font_face("Liberation Serif", cairo.FONT_SLANT_NORMAL, cairo.FONT_WEIGHT_NORMAL)
            ctx.set_font_size(21)
            ctx.set_source_rgba(C_SILVER[0], C_SILVER[1], C_SILVER[2], 0.98 * sc_alpha)
            ctx.move_to(rec_x + 95, rec_y + 80)
            ctx.show_text("The Silmarillion • Quenta Silmarillion (64% Read)")
            
            # Progress bar
            bar_w = 280
            bar_h = 8
            bx = rec_x + rec_w - bar_w - 50
            by = rec_y + 60
            ctx.set_source_rgba(0.2, 0.25, 0.35, 0.5 * sc_alpha)
            ctx.rectangle(bx, by, bar_w, bar_h)
            ctx.fill()
            
            # Progress fill (animated)
            p_fill = min(1.0, 0.64 + 0.05 * math.sin(t * 2.0))
            ctx.set_source_rgba(C_GOLD_LIGHT[0], C_GOLD_LIGHT[1], C_GOLD_LIGHT[2], 0.95 * sc_alpha)
            ctx.rectangle(bx, by, bar_w * p_fill, bar_h)
            ctx.fill()
            
            ctx.set_font_size(14)
            ctx.set_source_rgba(C_SILVER_MUTED[0], C_SILVER_MUTED[1], C_SILVER_MUTED[2], 0.85 * sc_alpha)
            ctx.move_to(bx, by + 28)
            ctx.show_text("High Cognitive Momentum • 4.2d Recency")

            ctx.restore()

        # -------------------------------------------------------------------
        # SCENE 3: CONSTELLATION OF THOUGHT (17.0s - 27.0s)
        # -------------------------------------------------------------------
        elif t < 27.2:
            sc_alpha = min(1.0, (t - 17.0) / 1.5) if t < 25.6 else max(0.0, (27.2 - t) / 1.5)
            ctx.save()
            
            # Header
            ctx.select_font_face("Liberation Serif", cairo.FONT_SLANT_NORMAL, cairo.FONT_WEIGHT_BOLD)
            ctx.set_font_size(36)
            hdr = "THE CONSTELLATION OF THOUGHT"
            ext = ctx.text_extents(hdr)
            ctx.set_source_rgba(C_SILVER[0], C_SILVER[1], C_SILVER[2], 0.95 * sc_alpha)
            ctx.move_to(960 - ext.width / 2.0, 130)
            ctx.show_text(hdr)
            
            ctx.select_font_face("Liberation Serif", cairo.FONT_SLANT_ITALIC, cairo.FONT_WEIGHT_NORMAL)
            ctx.set_font_size(19)
            sub = "Coulomb-Hooke Spring Physics • Dynamic Node Pulsing"
            s_ext = ctx.text_extents(sub)
            ctx.set_source_rgba(C_MITHRIL_TEAL[0], C_MITHRIL_TEAL[1], C_MITHRIL_TEAL[2], 0.88 * sc_alpha)
            ctx.move_to(960 - s_ext.width / 2.0, 168)
            ctx.show_text(sub)

            # Draw Spring Edges
            for u, v in graph.edges:
                nu = graph.nodes[u]
                nv = graph.nodes[v]
                ctx.set_source_rgba(C_MITHRIL_TEAL[0], C_MITHRIL_TEAL[1], C_MITHRIL_TEAL[2], 0.28 * sc_alpha)
                ctx.set_line_width(1.5)
                ctx.move_to(nu["x"], nu["y"])
                ctx.line_to(nv["x"], nv["y"])
                ctx.stroke()
                
                # Travelling energy pulse along the edge
                pulse_t = (t * 1.5 + (u + v) * 0.3) % 1.0
                px = nu["x"] + (nv["x"] - nu["x"]) * pulse_t
                py = nu["y"] + (nv["y"] - nu["y"]) * pulse_t
                ctx.set_source_rgba(C_GOLD_LIGHT[0], C_GOLD_LIGHT[1], C_GOLD_LIGHT[2], 0.85 * sc_alpha)
                ctx.arc(px, py, 2.5, 0, 2 * math.pi)
                ctx.fill()

            # Draw Nodes
            for idx, node in enumerate(graph.nodes):
                nx, ny = node["x"], node["y"]
                nr = node["r"]
                pulse = 1.0 + 0.12 * math.sin(t * 3.0 + idx * 1.1)
                
                # Outer glow
                ctx.set_source_rgba(C_MITHRIL_TEAL[0], C_MITHRIL_TEAL[1], C_MITHRIL_TEAL[2], 0.22 * sc_alpha)
                ctx.arc(nx, ny, nr * 1.8 * pulse, 0, 2 * math.pi)
                ctx.fill()
                
                # Inner disc
                ctx.set_source_rgba(0.08, 0.14, 0.24, 0.85 * sc_alpha)
                ctx.arc(nx, ny, nr * pulse, 0, 2 * math.pi)
                ctx.fill()
                
                # Golden progress ring
                prog_ang = node["prog"] * 2.0 * math.pi
                ctx.set_source_rgba(C_GOLD[0], C_GOLD[1], C_GOLD[2], 0.95 * sc_alpha)
                ctx.set_line_width(2.5)
                ctx.arc(nx, ny, nr * pulse, -math.pi / 2, -math.pi / 2 + prog_ang)
                ctx.stroke()
                
                # Central star
                draw_elven_star(ctx, nx, ny, radius=nr * 0.45 * pulse, rotation=t * 0.5 + idx, alpha=sc_alpha)
                
                # Label
                ctx.select_font_face("Liberation Serif", cairo.FONT_SLANT_NORMAL, cairo.FONT_WEIGHT_BOLD)
                ctx.set_font_size(15)
                lbl = node["name"]
                l_ext = ctx.text_extents(lbl)
                ctx.set_source_rgba(C_SILVER[0], C_SILVER[1], C_SILVER[2], 0.95 * sc_alpha)
                ctx.move_to(nx - l_ext.width / 2.0, ny + nr * pulse + 24)
                ctx.show_text(lbl)

            # Mathematical Spec HUD Badge (bottom right)
            hud_w = 420
            hud_h = 110
            hud_x = WIDTH - hud_w - 90
            hud_y = HEIGHT - hud_h - 90
            draw_glass_card(ctx, hud_x, hud_y, hud_w, hud_h, radius=14, alpha=sc_alpha)
            
            ctx.select_font_face("Liberation Serif", cairo.FONT_SLANT_NORMAL, cairo.FONT_WEIGHT_BOLD)
            ctx.set_font_size(14)
            ctx.set_source_rgba(C_GOLD[0], C_GOLD[1], C_GOLD[2], 0.95 * sc_alpha)
            ctx.move_to(hud_x + 25, hud_y + 35)
            ctx.show_text("FORCE-DIRECTED GRAPH PHYSICS")
            
            ctx.select_font_face("Liberation Serif", cairo.FONT_SLANT_NORMAL, cairo.FONT_WEIGHT_NORMAL)
            ctx.set_font_size(14)
            ctx.set_source_rgba(C_SILVER_MUTED[0], C_SILVER_MUTED[1], C_SILVER_MUTED[2], 0.88 * sc_alpha)
            ctx.move_to(hud_x + 25, hud_y + 65)
            ctx.show_text("Coulomb Repulsion: 12,000 | Spring Stiffness: 0.04")
            ctx.move_to(hud_x + 25, hud_y + 88)
            ctx.show_text("Simulated Annealing • Pure Kotlin Math")

            ctx.restore()

        # -------------------------------------------------------------------
        # SCENE 4: THE ETHEREAL READER & FOCUS MODE (27.0s - 36.0s)
        # -------------------------------------------------------------------
        elif t < 36.2:
            sc_alpha = min(1.0, (t - 27.0) / 1.5) if t < 34.6 else max(0.0, (36.2 - t) / 1.5)
            ctx.save()
            
            # Transition into Pure Focus Mode: Chrome fades, Prose emerges
            focus_progress = min(1.0, max(0.0, (t - 29.0) / 2.0))
            
            # Reader Parchment Panel
            pw = 920
            ph = 680
            px = (WIDTH - pw) / 2.0
            py = 180
            draw_glass_card(ctx, px, py, pw, ph, radius=20, alpha=sc_alpha)
            
            # Reader Top Chrome (Fades as Focus Mode Engages)
            top_alpha = sc_alpha * (1.0 - focus_progress * 0.85)
            ctx.select_font_face("Liberation Serif", cairo.FONT_SLANT_NORMAL, cairo.FONT_WEIGHT_BOLD)
            ctx.set_font_size(16)
            ctx.set_source_rgba(C_GOLD[0], C_GOLD[1], C_GOLD[2], 0.95 * top_alpha)
            ctx.move_to(px + 40, py + 50)
            ctx.show_text("THE ETHEREAL READER • CHAPTER IV: OF TÚRIN TURAMBAR")
            
            ctx.select_font_face("Liberation Serif", cairo.FONT_SLANT_ITALIC, cairo.FONT_WEIGHT_NORMAL)
            ctx.set_font_size(14)
            ctx.set_source_rgba(C_MITHRIL_TEAL[0], C_MITHRIL_TEAL[1], C_MITHRIL_TEAL[2], 0.85 * top_alpha)
            ctx.move_to(px + pw - 240, py + 50)
            ctx.show_text("Adaptive Daylight • 265 WPM")

            # Divider
            ctx.set_source_rgba(C_SILVER_MUTED[0], C_SILVER_MUTED[1], C_SILVER_MUTED[2], 0.25 * top_alpha)
            ctx.set_line_width(1.0)
            ctx.move_to(px + 40, py + 75)
            ctx.line_to(px + pw - 40, py + 75)
            ctx.stroke()
            
            # Book Text (Timeless Serif Prose)
            ctx.select_font_face("Liberation Serif", cairo.FONT_SLANT_NORMAL, cairo.FONT_WEIGHT_NORMAL)
            ctx.set_font_size(24)
            
            # Golden Drop Cap
            ctx.select_font_face("Liberation Serif", cairo.FONT_SLANT_NORMAL, cairo.FONT_WEIGHT_BOLD)
            ctx.set_font_size(52)
            ctx.set_source_rgba(C_GOLD_LIGHT[0], C_GOLD_LIGHT[1], C_GOLD_LIGHT[2], 0.98 * sc_alpha)
            ctx.move_to(px + 50, py + 145)
            ctx.show_text("T")
            
            # Body Paragraphs
            ctx.select_font_face("Liberation Serif", cairo.FONT_SLANT_NORMAL, cairo.FONT_WEIGHT_NORMAL)
            ctx.set_font_size(22)
            ctx.set_source_rgba(C_SILVER[0], C_SILVER[1], C_SILVER[2], 0.92 * sc_alpha)
            
            prose_lines = [
                "  here was music in the halls of Menegroth, yet in the heart",
                "of the hidden realm there lay a quiet sanctuary where time",
                "stood still. The stars above the garden trees were cold and clear,",
                "and ancient memory dwelt within every leaf and stone.",
                "",
                "“Deep roots are not reached by the frost; the old that is strong",
                "does not wither. From the shadows of the forest, a quiet light",
                "shall spring, and wisdom gathered in silence shall outlive",
                "the noise of all the ages of the world.”"
            ]
            
            y_offset = py + 135
            for idx, line in enumerate(prose_lines):
                if line.startswith("“"):
                    # Highlighted Resurfaced Wisdom
                    ctx.set_source_rgba(C_GOLD_LIGHT[0], C_GOLD_LIGHT[1], C_GOLD_LIGHT[2], 0.98 * sc_alpha)
                    ctx.select_font_face("Liberation Serif", cairo.FONT_SLANT_ITALIC, cairo.FONT_WEIGHT_NORMAL)
                elif line == "":
                    y_offset += 16
                    continue
                else:
                    ctx.set_source_rgba(C_SILVER[0], C_SILVER[1], C_SILVER[2], 0.90 * sc_alpha)
                    ctx.select_font_face("Liberation Serif", cairo.FONT_SLANT_NORMAL, cairo.FONT_WEIGHT_NORMAL)
                    
                ctx.move_to(px + 90, y_offset)
                ctx.show_text(line)
                y_offset += 40

            # Resurfaced Memory Recall Leaf Pill (Floating upwards)
            pill_y = py + ph - 130 + math.sin(t * 2.2) * 6.0
            pill_w = 640
            pill_x = px + (pw - pill_w) / 2.0
            draw_glass_card(ctx, pill_x, pill_y, pill_w, 75, radius=37, alpha=sc_alpha)
            
            draw_elven_star(ctx, pill_x + 45, pill_y + 37, radius=16.0, rotation=t * 0.4, alpha=sc_alpha)
            
            ctx.select_font_face("Liberation Serif", cairo.FONT_SLANT_NORMAL, cairo.FONT_WEIGHT_BOLD)
            ctx.set_font_size(14)
            ctx.set_source_rgba(C_GOLD[0], C_GOLD[1], C_GOLD[2], 0.95 * sc_alpha)
            ctx.move_to(pill_x + 80, pill_y + 32)
            ctx.show_text("MEMORY RECALL • RESURFACED AT TWILIGHT")
            
            ctx.select_font_face("Liberation Serif", cairo.FONT_SLANT_ITALIC, cairo.FONT_WEIGHT_NORMAL)
            ctx.set_font_size(15)
            ctx.set_source_rgba(C_SILVER[0], C_SILVER[1], C_SILVER[2], 0.95 * sc_alpha)
            ctx.move_to(pill_x + 80, pill_y + 55)
            ctx.show_text("“Deep roots are not reached by the frost.”")

            ctx.restore()

        # -------------------------------------------------------------------
        # SCENE 5: SOVEREIGN SANCTUARY & CALL TO ACTION (36.0s - 45.0s)
        # -------------------------------------------------------------------
        else:
            sc_alpha = min(1.0, (t - 36.0) / 1.5) if t < 43.5 else max(0.0, (45.0 - t) / 1.5)
            ctx.save()
            
            # Central Star of Rivendell (Large, majestic radiance)
            draw_elven_star(ctx, 960, 270, radius=110.0, rotation=t * 0.15, alpha=sc_alpha)
            
            # App icon floating in star center
            if app_icon_surface:
                ctx.save()
                icon_w = app_icon_surface.get_width()
                icon_h = app_icon_surface.get_height()
                ctx.translate(960, 270)
                ctx.scale(0.42, 0.42)
                ctx.set_source_surface(app_icon_surface, -icon_w / 2.0, -icon_h / 2.0)
                ctx.paint_with_alpha(sc_alpha * 0.95)
                ctx.restore()

            # Main Anthem Title
            ctx.select_font_face("Liberation Serif", cairo.FONT_SLANT_NORMAL, cairo.FONT_WEIGHT_BOLD)
            ctx.set_font_size(58)
            title = "I M L A D R I S"
            ext = ctx.text_extents(title)
            ctx.set_source_rgba(C_SILVER[0], C_SILVER[1], C_SILVER[2], 0.98 * sc_alpha)
            ctx.move_to(960 - ext.width / 2.0, 450)
            ctx.show_text(title)
            
            ctx.select_font_face("Liberation Serif", cairo.FONT_SLANT_ITALIC, cairo.FONT_WEIGHT_NORMAL)
            ctx.set_font_size(24)
            sub = "Your Sovereign Mind Palace"
            s_ext = ctx.text_extents(sub)
            ctx.set_source_rgba(C_GOLD_LIGHT[0], C_GOLD_LIGHT[1], C_GOLD_LIGHT[2], 0.90 * sc_alpha)
            ctx.move_to(960 - s_ext.width / 2.0, 495)
            ctx.show_text(sub)

            # Three Sovereign Architectural Badges
            badges = [
                "100% OFFLINE-FIRST",
                "ZERO TELEMETRY",
                "PURE KOTLIN PHYSICS"
            ]
            badge_w = 260
            badge_h = 56
            start_bx = 960 - (3 * badge_w + 2 * 30) / 2.0
            
            for b_idx, b_text in enumerate(badges):
                bx = start_bx + b_idx * (badge_w + 30)
                by = 545
                draw_glass_card(ctx, bx, by, badge_w, badge_h, radius=14, alpha=sc_alpha)
                
                ctx.select_font_face("Liberation Serif", cairo.FONT_SLANT_NORMAL, cairo.FONT_WEIGHT_BOLD)
                ctx.set_font_size(15)
                b_ext = ctx.text_extents(b_text)
                ctx.set_source_rgba(C_MITHRIL_TEAL[0], C_MITHRIL_TEAL[1], C_MITHRIL_TEAL[2], 0.95 * sc_alpha)
                ctx.move_to(bx + (badge_w - b_ext.width) / 2.0, by + 34)
                ctx.show_text(b_text)

            # Terminal Clone Command Card (Clean & Elegant)
            cmd_w = 780
            cmd_h = 90
            cmd_x = (WIDTH - cmd_w) / 2.0
            cmd_y = 650
            draw_glass_card(ctx, cmd_x, cmd_y, cmd_w, cmd_h, radius=18, alpha=sc_alpha)
            
            ctx.select_font_face("Liberation Serif", cairo.FONT_SLANT_NORMAL, cairo.FONT_WEIGHT_BOLD)
            ctx.set_font_size(14)
            ctx.set_source_rgba(C_GOLD[0], C_GOLD[1], C_GOLD[2], 0.90 * sc_alpha)
            ctx.move_to(cmd_x + 35, cmd_y + 35)
            ctx.show_text("BUILD & ASSEMBLE")
            
            ctx.select_font_face("Liberation Mono", cairo.FONT_SLANT_NORMAL, cairo.FONT_WEIGHT_BOLD)
            ctx.set_font_size(19)
            cmd_text = "git clone https://github.com/kisalnelaka/imladris.git"
            ctx.set_source_rgba(C_SILVER[0], C_SILVER[1], C_SILVER[2], 0.98 * sc_alpha)
            ctx.move_to(cmd_x + 35, cmd_y + 68)
            ctx.show_text(cmd_text)

            # Closing Tolkien Blessing
            ctx.select_font_face("Liberation Serif", cairo.FONT_SLANT_ITALIC, cairo.FONT_WEIGHT_NORMAL)
            ctx.set_font_size(19)
            blessing = "“May the stars shine upon the hour of your meeting.”"
            bl_ext = ctx.text_extents(blessing)
            ctx.set_source_rgba(C_SILVER_MUTED[0], C_SILVER_MUTED[1], C_SILVER_MUTED[2], 0.85 * sc_alpha)
            ctx.move_to(960 - bl_ext.width / 2.0, 790)
            ctx.show_text(blessing)

            ctx.restore()

        # Stream raw frame buffer directly to ffmpeg
        proc.stdin.write(surface.get_data())
        
        # Log progress every 300 frames (~5s)
        if frame_idx % 300 == 0:
            pct = int((frame_idx / float(TOTAL_FRAMES)) * 100)
            print(f"  -> Rendered frame {frame_idx}/{TOTAL_FRAMES} ({pct}%)")

    # Finalize ffmpeg encoding
    proc.stdin.close()
    proc.wait()
    ffmpeg_log.close()
    
    if proc.returncode != 0:
        with open("/tmp/ffmpeg_render.log", "r") as f:
            err = f.read()
        print(f"FFmpeg failed with exit code {proc.returncode}:\n{err}")
        sys.exit(1)
        
    print(f"[Done] Launch video created successfully: {OUTPUT_VIDEO}")
    print(f"File size: {os.path.getsize(OUTPUT_VIDEO) / (1024 * 1024):.2f} MB")

if __name__ == "__main__":
    render_launch_video()
