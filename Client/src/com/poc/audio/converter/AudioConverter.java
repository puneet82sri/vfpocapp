package com.poc.audio.converter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;


//import javax.sound.midi.Track;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import net.sourceforge.jaad.mp4.MP4Container;
import net.sourceforge.jaad.mp4.api.AudioTrack;
import net.sourceforge.jaad.mp4.api.Frame;
import net.sourceforge.jaad.mp4.api.Movie;
import net.sourceforge.jaad.mp4.api.Track;
import net.sourceforge.jaad.mp4.od.DecoderSpecificInfo;
import net.sourceforge.jaad.spi.javasound.AACAudioFileReader;

public class AudioConverter {

	public static void main(String[] args) {
		
		System.out.println("Process result : " + new AudioConverter().execute());
		
	}
	
	
	public boolean execute()
	{
		boolean result = true;
		
		//saveTheFileFromURL("https://cdn.fbsbx.com/v/t59.3654-21/16258619_1632119233481718_729472179153928192_n.mp4/audioclip-1486684066000-3030.mp4?oh=8012e954a39367570c6c305dd07d9f07&oe=589EFF9F");
		String str;
		
		
//		formatURL("https:\/\/cdn.fbsbx.com\/v\/t59.3654-21\/16258619_1632119233481718_729472179153928192_n.mp4\/audioclip-1486684066000-3030.mp4?oh=8012e954a39367570c6c305dd07d9f07&oe=589EFF9F");
		
//		readAacFile("C:\\E\\POC\\Nuance\\voice\\fb\\verify-browser.wav");
//		readFile("C:\\E\\POC\\Nuance\\voice\\fb\\verify-browser.wav");
		
//		readAacFile("C:\\E\\POC\\Nuance\\voice\\example.aac");
//		readAacFile("C:\\E\\POC\\Nuance\\voice\\fb\\fbvoice2.aac");
		
		return result;
	}
	
	
	private void readFile(String filePath)
	{
		int totalFramesRead = 0;
		File fileIn = new File(filePath);
		// somePathName is a pre-existing string whose value was
		// based on a user selection.
		try {
			
//			
//			AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, //The type of encoding for the audio stream
//                    44100, //Sample rate of the audio stream
//                    32, //Number of bits in each sample of a sound that has this format.
//                    1,   //Number of audio channels in this audio stream
//                    1 * 2,   //Number of bytes in each frame of the audiostream
//                    44100, //Number of frames played or recorded per second, for the sound in the audio stream
//                    false); //Data stored in little-endian order
			
//			AudioInputStream decodedAudioStream = AudioSystem.getAudioInputStream(decodedFormat, inputStream);
			
			
			AudioFileFormat audioFileFormat = AudioSystem.getAudioFileFormat(new File(filePath));
			System.out.println("pun audioFileFormat : " + audioFileFormat);
			
			
			  AudioInputStream audioInputStream = 
			    AudioSystem.getAudioInputStream(fileIn);
			
			
			
			
			
			
			
			Type[] audioFileTypes = AudioSystem.getAudioFileTypes(audioInputStream);
			for (Type type : audioFileTypes)
			{
				System.out.println("getAudioFileTypes for stream: " + type.getExtension());
			}
			
			
			
//		  int bytesPerFrame = 
//		    audioInputStream.getFormat().getFrameSize();
//		    if (bytesPerFrame == AudioSystem.NOT_SPECIFIED) {
//		    // some audio formats may have unspecified frame size
//		    // in that case we may read any amount of bytes
//		    bytesPerFrame = 1;
//		  } 
//		  // Set an arbitrary buffer size of 1024 frames.
//		  int numBytes = 1024 * bytesPerFrame; 
//		  byte[] audioBytes = new byte[numBytes];
//		 
//		    int numBytesRead = 0;
//		    int numFramesRead = 0;
//		    // Try to read numBytes bytes from the file.
//		    while ((numBytesRead = 
//		      audioInputStream.read(audioBytes)) != -1) {
//		      // Calculate the number of frames actually read.
//		      numFramesRead = numBytesRead / bytesPerFrame;
//		      totalFramesRead += numFramesRead;
//		      // Here, do something useful with the audio data that's 
//		      // now in the audioBytes array...
//		    }
		  
		} catch (Exception e) {
		  // Handle the error...
			e.printStackTrace();
		}
	}
	
	
	public void processMP4(InputStream inputStream) {
		MP4Container container = null;
		try {
			container = new MP4Container(inputStream);
			Movie movie = container.getMovie();
			List<Track> tracks = movie.getTracks(AudioTrack.AudioCodec.AAC);
			if (tracks.size() > 0) {
				Track track = tracks.get(0);
				byte[] decoderSpecificInfo = track.getDecoderSpecificInfo();
				Frame frame = track.readNextFrame();
				byte[] data = frame.getData();
				// do something with the frame, e.g. pass it to the AAC decoder
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void readAacFile(String filePath)
	{
		DecoderSpecificInfo decoderSpecificInfo = new DecoderSpecificInfo();
//		decoderSpecificInfo.
		
		
		AACAudioFileReader aacreader = new AACAudioFileReader();
		AudioFileFormat audioFileFormat = null;
		try {
			File file = new File(filePath);
			audioFileFormat = aacreader.getAudioFileFormat(file);
			
			
			System.out.println("audioFileFormat : " + audioFileFormat);
//			AudioInputStream inputStream = aacreader.getAudioInputStream(file);
//			System.out.println("inputStream : " + inputStream);
//			
//			
//			AudioFormat baseFormat = inputStream.getFormat();    //Obtains the audio format of the song in the audio input stream.
//			System.out.println("baseFormat : " + baseFormat);
			
//			AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, //The type of encoding for the audio stream
//                    44100, //Sample rate of the audio stream
//                    32, //Number of bits in each sample of a sound that has this format.
//                    1,   //Number of audio channels in this audio stream
//                    1 * 2,   //Number of bytes in each frame of the audiostream
//                    44100, //Number of frames played or recorded per second, for the sound in the audio stream
//                    false); //Data stored in little-endian order
//			
//			AudioInputStream decodedAudioStream = AudioSystem.getAudioInputStream(decodedFormat, inputStream);
//			System.out.println("decodedFormat : " + decodedFormat);

//			Type[] audioFileTypes = AudioSystem.getAudioFileTypes(inputStream);
//			for (Type type : audioFileTypes)
//				System.out.println("type : " + type);
			
			
			
//			File fileOut = new File("C:\\E\\POC\\Nuance\\voice\\fb\\fbvoice2.wav");
//			//AudioFileFormat.Type fileType = fileFormat.getType();
//			AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;
//			if (AudioSystem.isFileTypeSupported(fileType, 
//			    inputStream)) {
//			  AudioSystem.write(inputStream, fileType, fileOut);
//			}
		} catch (UnsupportedAudioFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		aacreader.get
	}
	
	public String formatURL(String url)
	{
		String finalStr = "";
		
		finalStr = url;
		
		return finalStr;
	}
	
	
	
	public boolean saveTheFileFromURL(String voiceURL)
	{
		boolean success = true;
		try {
			URL url = new URL(voiceURL);
			URLConnection connection = url.openConnection();
			InputStream in = connection.getInputStream();
			FileOutputStream fos = new FileOutputStream(new File("c:\\voiceBiometrics\\myvoice.wav"));
			byte[] buf = new byte[512];
			while (true) {
			    int len = in.read(buf);
			    if (len == -1) {
			        break;
			    }
			    fos.write(buf, 0, len);
			}
			in.close();
			fos.flush();
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
			success = false;
		}
		return success;
	}
	

}
