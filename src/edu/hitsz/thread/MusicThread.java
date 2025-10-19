package edu.hitsz.thread;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.DataLine.Info;

public class MusicThread extends Thread {


    //音频文件名
    private String filename;
    private AudioFormat audioFormat;
    private byte[] samples;

    private volatile boolean isStopped = false;
    private final boolean isLoop;

    public MusicThread(String filename, boolean isLoop) {
        //初始化filename
        this.filename = filename;
        this.isLoop = isLoop;
        reverseMusic();
    }

    public void stopMusic() {
        isStopped = true;
    }

    public void reverseMusic() {
        try {
            //定义一个AudioInputStream用于接收输入的音频数据，使用AudioSystem来获取音频的音频输入流
            AudioInputStream stream = AudioSystem.getAudioInputStream(new File(filename));
            //用AudioFormat来获取AudioInputStream的格式
            audioFormat = stream.getFormat();
            samples = getSamples(stream);
        } catch (UnsupportedAudioFileException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public byte[] getSamples(AudioInputStream stream) {
        // 音频帧数 * 每帧字节数
        int size = (int) (stream.getFrameLength() * audioFormat.getFrameSize());
        byte[] samples = new byte[size];
        DataInputStream dataInputStream = new DataInputStream(stream);
        try {
            dataInputStream.readFully(samples);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return samples;
    }

    @Override
    public void run() {
        // 1. 准备阶段
        SourceDataLine dataLine = null; // 先声明一个空的“管道”变量
        try {
            // --- 这部分是获取并设置“管道” ---

            // 1a. 描述你想要的管道规格
            // AudioFormat 包含了音频的格式信息（采样率、声道数等），是从音频文件中读取的。
            // 我们用这个格式信息来告诉系统：“我需要一个能处理这种格式音频的管道”。
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);

            // 1b. 向音频系统申请管道
            // AudioSystem 是一个工厂，我们把规格(info)给它，它就会返回一个符合要求的实例。
            // 这就好比告诉五金店老板：“我需要一根能接我家水龙头的水管”，老板就给你找了一根合适的。
            dataLine = (SourceDataLine) AudioSystem.getLine(info);

            // 1c. 打开管道
            // 获取管道后，需要打开它。这一步会为播放分配必要的系统资源。
            // 就像把水管接到水龙头上，准备好随时可以放水。
            dataLine.open(audioFormat);

            // 1d. 启动管道
            // 告诉管道可以开始接收和处理数据了。
            // 就像打开洒水器的开关，它已经准备好，只要有水过来就能洒出去。
            dataLine.start();

            // 2. 核心播放循环
            do {
                // 2a. 准备数据源
                // samples 是一个 byte 数组，包含了整个音频文件的所有数据，像一个装满了水的水桶。
                // ByteArrayInputStream 把这个数组包装成一个输入流，方便我们一瓢一瓢地读取。
                InputStream stream = new ByteArrayInputStream(samples);

                // 2b. 准备一个“水瓢”
                // buffer 是一个缓冲区，我们不用一个字节一个字节地传输，而是用这个“水瓢”一次取一部分，效率更高。
                byte[] buffer = new byte[1024];
                int bytesRead = 0;

                // 2c. 开始舀水并输送
                // 这个 while 循环是播放一次音乐的核心。
                // stream.read(buffer) 从“水桶”里舀一瓢水（最多1024字节）到“水瓢”里，并返回实际舀到的水量 (bytesRead)。
                // 如果水舀完了 (stream.read 返回 -1)，或者外部调用了 stopMusic() (isStopped 变为 true)，循环就结束。
                while ((bytesRead = stream.read(buffer)) != -1 && !isStopped) {
                    // 这就是最关键的一步：将“水瓢”里的水倒入“管道”。
                    // dataLine.write 将 buffer 中的音频数据写入管道，然后声卡就会处理这些数据并从扬声器播放出来。
                    dataLine.write(buffer, 0, bytesRead);
                }

            } while (isLoop && !isStopped); // 如果设置了循环播放(isLoop)，并且没有被中途停止，就回到 do 再来一次。

        } catch (LineUnavailableException | IOException e) {
            // 如果在准备或播放过程中出错（比如声卡被占用），打印错误信息。
            e.printStackTrace();
        } finally {
            // 3. 清理阶段 (无论成功还是失败，都必须执行)
            if (dataLine != null) {
                // 3a. 等待管道中的数据播放完毕
                // drain() 方法会阻塞程序，直到所有已经写入管道但还没播放完的数据全部播放完毕。
                // 这可以防止音乐在末尾被突然切断。就像确保水管里最后一点水也从洒水器流出。
                dataLine.drain();

                // 3b. 关闭管道
                // 释放所有占用的系统资源。就像关掉水龙头并拆下水管。
                dataLine.close();
            }
        }
    }

//    public void play(InputStream source) {
//        int size = (int) (audioFormat.getFrameSize() * audioFormat.getSampleRate());
//        byte[] buffer = new byte[size];
//        //源数据行SoureDataLine是可以写入数据的数据行
//        SourceDataLine dataLine = null;
//        //获取受数据行支持的音频格式DataLine.info
//        Info info = new Info(SourceDataLine.class, audioFormat);
//        try {
//            dataLine = (SourceDataLine) AudioSystem.getLine(info);
//            dataLine.open(audioFormat, size);
//        } catch (LineUnavailableException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        dataLine.start();
//        try {
//            int numBytesRead = 0;
//            while (numBytesRead != -1) {
//				//从音频流读取指定的最大数量的数据字节，并将其放入缓冲区中
//                numBytesRead =
//                        source.read(buffer, 0, buffer.length);
//				//通过此源数据行将数据写入混频器
//                if (numBytesRead != -1) {
//                    dataLine.write(buffer, 0, numBytesRead);
//                }
//            }
//
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
//
//        dataLine.drain();
//        dataLine.close();
//
//    }
//
//    @Override
//    public void run() {
//        InputStream stream = new ByteArrayInputStream(samples);
//        play(stream);
//    }
}


