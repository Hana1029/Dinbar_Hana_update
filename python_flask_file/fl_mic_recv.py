# https://gist.github.com/mirfan899/4dec738636c711d378eac1793ec95329
#!/usr/bin/env python

import pyaudio
import socket
import sys
import wave
import time

def genHeader(sampleRate, bitsPerSample, channels):
    datasize = 2000*10**6
    o = bytes("RIFF",'ascii')                                               # (4byte) Marks file as RIFF
    o += (datasize + 36).to_bytes(4,'little')                               # (4byte) File size in bytes excluding this and RIFF marker
    o += bytes("WAVE",'ascii')                                              # (4byte) File type
    o += bytes("fmt ",'ascii')                                              # (4byte) Format Chunk Marker
    o += (16).to_bytes(4,'little')                                          # (4byte) Length of above format data
    o += (1).to_bytes(2,'little')                                           # (2byte) Format type (1 - PCM)
    o += (channels).to_bytes(2,'little')                                    # (2byte)
    o += (sampleRate).to_bytes(4,'little')                                  # (4byte)
    o += (sampleRate * channels * bitsPerSample // 8).to_bytes(4,'little')  # (4byte)
    o += (channels * bitsPerSample // 8).to_bytes(2,'little')               # (2byte)
    o += (bitsPerSample).to_bytes(2,'little')                               # (2byte)
    o += bytes("data",'ascii')                                              # (4byte) Data Chunk Marker
    o += (datasize).to_bytes(4,'little')                                    # (4byte) Data size in bytes
    return o

def audio_receive():
    global stopflag
    stopflag = False
    FORMAT = pyaudio.paInt16
    CHANNELS = 1
    RATE = 8000
    CHUNK = 2048
    bitsPerSample = 16
    
    host_ip = '192.168.137.10'
    port = 9999
    socket_address = (host_ip,port)

    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.connect(socket_address)
    audio = pyaudio.PyAudio()
    stream = audio.open(format=FORMAT, channels=CHANNELS, rate=RATE, output=True, frames_per_buffer=CHUNK)


    #wav_header = genHeader(RATE, bitsPerSample, CHANNELS)
    try:
        first_run = True
        while True:    # ?????????python??????
            data = s.recv(CHUNK)
            stream.write(data)
            if stopflag:
                break
        # while True:   # ??????iframe??? ????????????
        #     if first_run:
        #         data = wav_header + s.recv(CHUNK)
        #         first_run = False
        #     else:
        #         data = s.recv(CHUNK)
        #     yield(data)
    except KeyboardInterrupt:
        pass
    except Exception as e:
        print(e)

    print('??????????????????')
    s.close()
    stream.close()
    audio.terminate()


def audio_record():
    save_path = "../../save_audio/"
    timeString = time.strftime("%Y_%m_%d_%H_%M_%S", time.localtime())
    filename = save_path + timeString + ".wav"
    
    global stopflag
    stopflag = False
    FORMAT = pyaudio.paInt16
    CHANNELS = 1
    RATE = 8000
    CHUNK = 2048
    bitsPerSample = 16
    
    host_ip = '192.168.137.10'
    port = 9999
    socket_address = (host_ip,port)

    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.connect(socket_address)
    audio = pyaudio.PyAudio()
    stream = audio.open(format=FORMAT, channels=CHANNELS, rate=RATE, output=True, frames_per_buffer=CHUNK)


    frames = []                         #????????????
    try:
        while True:
            data = s.recv(CHUNK)
            stream.write(data)          # ??????
            frames.append(data)          # ???????????????????????????
            if stopflag:
                break
    except KeyboardInterrupt:
        pass
    except Exception as e:
        print(e)

    print('??????????????????, ?????????')


    wf = wave.open(filename, 'wb')   # ?????????????????????
    wf.setnchannels(CHANNELS)        # ????????????
    wf.setsampwidth(audio.get_sample_size(FORMAT))  # ????????????
    wf.setframerate(RATE)              # ??????????????????
    wf.writeframes(b''.join(frames)) # ??????
    wf.close()
    s.close()
    
    stream.close()
    audio.terminate()