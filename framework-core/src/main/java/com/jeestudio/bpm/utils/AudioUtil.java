package com.jeestudio.bpm.utils;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @Description: 音频处理工具
 */
public class AudioUtil {

    private static final Logger logger = LoggerFactory.getLogger(AudioUtil.class);

    public static JSONObject getHeadInfo(String filePath){
        String type = filePath.substring(filePath.lastIndexOf(".") + 1);
        JSONObject result = null;
        if (type.equals("mp3")){
            result = getHeadInfoFromMp3(filePath);
        } else if (type.equals("wav")) {
            result = getHeadInfoFromWav(filePath);
        }
        return result;
    }

    /**
     * 从WAV文件中提取音频头信息
     * 该方法读取WAV文件的音频头信息，并将相关信息（如采样率、采样位数、时长和等效连续声级）封装到JSONObject中
     *
     * @param filePath WAVE文件的路径
     * @return 包含音频头信息的JSONObject，包括采样率sampleRate、采样位数sampleSize、时长duration和等效连续声级leq
     */
    public static JSONObject getHeadInfoFromWav(String filePath){
        JSONObject result = new JSONObject();

        try(AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(filePath))) {

            AudioFormat format = audioInputStream.getFormat();

            // 采样率（单位：kHz）
            float sampleRate = format.getSampleRate() / 1000.0f;

            // 采样位数
            int sampleSize = format.getSampleSizeInBits();

            // 计算时长（秒）
            long frames = audioInputStream.getFrameLength();
            double duration = (frames + 0.0) / format.getFrameRate();

            result.put("sampleRate", sampleRate);
            result.put("sampleSize", sampleSize);
            result.put("duration", duration);
            result.put("leq", getAudioLeq(audioInputStream));
        }catch (Exception e){
            logger.warn("获取WAV文件头信息失败, filePath: {}", filePath, e);
        }

        return result;
    }

    /**
     * 从MP3文件中提取头部信息
     * 该方法读取MP3文件的音频流，将其转换为PCM格式，并将相关信息（如采样率、采样位数、时长和等效连续声级）封装到JSONObject中
     *
     * @param filePath MP3文件的路径
     * @return 包含MP3文件头部信息的JSONObject，包括采样率sampleRate、采样位数sampleSize、时长duration和等效连续声级leq
     */
    public static JSONObject getHeadInfoFromMp3(String filePath){
        JSONObject result = new JSONObject();

        try (AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(filePath))) {
            // 获取转换后的PCM格式
            AudioFormat baseFormat = audioInputStream.getFormat();
            AudioFormat decodedFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    baseFormat.getSampleRate(),
                    16,
                    baseFormat.getChannels(),
                    baseFormat.getChannels() * 2,
                    baseFormat.getSampleRate(),
                    false
            );
            // 通过转换流获取真实参数
            try (AudioInputStream pcmStream = AudioSystem.getAudioInputStream(decodedFormat, audioInputStream)) {
                AudioFormat format = pcmStream.getFormat();

                // 采样率（单位：kHz）
                float sampleRate = format.getSampleRate() / 1000.0f;
                BigDecimal roundedSampleRate = new BigDecimal(sampleRate).setScale(1, RoundingMode.HALF_UP);

                // MP3固定为压缩数据，实际采样位数需从解码后获取
                int sampleSize = format.getSampleSizeInBits();

                // 计算时长
                long frames = format.getFrameSize();
                double duration = (frames + 0.0) / format.getFrameRate();

                result.put("sampleRate", roundedSampleRate);
                result.put("sampleSize", sampleSize);
                result.put("duration", duration);
                result.put("leq", getAudioLeq(pcmStream));
            } catch (IOException e) {
                logger.warn("获取MP3 PCM流信息失败, filePath: {}", filePath, e);
            }
        } catch (Exception e){
            logger.warn("获取MP3文件头信息失败, filePath: {}", filePath, e);
        }

        return result;
    }

    /**
     * 计算音频的等效连续A声级（Leq）
     * 该方法通过计算音频样本的均方根（RMS）电平，然后将其转换为分贝（dB）来实现
     *
     * @param audioInputStream 音频输入流，用于读取音频数据
     * @return 返回计算得到的等效连续A声级（Leq）
     * @throws IOException 当读取音频输入流时发生错误
     */
    public static double getAudioLeq(AudioInputStream audioInputStream) throws IOException {
        byte[] buffer = new byte[4096];
        long totalSamples = 0;
        double sumSquares = 0.0;

        while (audioInputStream.read(buffer) != -1) {
            // 转换为16位PCM样本
            for (int i = 0; i < buffer.length; i += 2) {
                short sample = (short) ((buffer[i + 1] << 8) | (buffer[i] & 0xFF));
                double normalized = sample ;
                sumSquares += normalized * normalized;
                totalSamples++;
            }
        }

        double rms = Math.sqrt(sumSquares / totalSamples);

        return 20 * Math.log10(rms);
    }
}
