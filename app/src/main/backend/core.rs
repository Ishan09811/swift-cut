use ffmpeg_next as ffmpeg;
use ffmpeg::{
    codec::Context as CodecContext,
    media::Type,
};
use ffmpeg::format::input;
use ffmpeg::software::scaling::{context::Context as Scaler, flag::Flags};
use ffmpeg::util::frame::video::Video;

pub fn extract_thumbnails(video: &str, out_dir: &str) -> Result<(), String> {
    ffmpeg::init().map_err(|e| e.to_string())?;

    let mut ictx = input(&video).map_err(|e| e.to_string())?;
    let input_stream = ictx.streams().best(Type::Video).ok_or("No video stream")?;

    let idx = input_stream.index();
    let duration_us = input_stream.duration();

    let count = ((duration_us / 1000) / 500).max(1) as i32;

    let codec_ctx = CodecContext::from_parameters(input_stream.parameters())?;
    let mut decoder = codec_ctx.decoder().video().unwrap();

    std::fs::create_dir_all(out_dir).unwrap();

    for i in 0..count {
        let ts = duration_us * i as i64 / count as i64;
        ictx.seek(ts, ..).map_err(|e| e.to_string())?;
        decoder.flush();

        let mut got = false;
        for (stream, packet) in ictx.packets() {
            if stream.index() != idx {
                continue;
            }

            decoder.send_packet(&packet).unwrap();

            let mut frame = Video::empty();
            if decoder.receive_frame(&mut frame).is_ok() {
                let mut rgb = Video::empty();
                let mut scaler = Scaler::get(
                    decoder.format(),
                    decoder.width(),
                    decoder.height(),
                    ffmpeg::format::Pixel::RGB24,
                    160,
                    90,
                    Flags::FAST_BILINEAR,
                ).unwrap();

                scaler.run(&frame, &mut rgb).unwrap();

                let out = format!("{}/thumb_{}.ppm", out_dir, i);
                save_ppm(&rgb, &out)?;

                got = true;
                break;
            }
        }

        if !got {
            return Err(format!("Failed to decode at index {}", i));
        }
    }

    Ok(())
}

fn save_ppm(frame: &Video, path: &str) -> Result<(), String> {
    let mut file = std::fs::File::create(path).map_err(|e| e.to_string())?;

    let header = format!("P6\n{} {}\n255\n", frame.width(), frame.height());
    std::io::Write::write_all(&mut file, header.as_bytes()).map_err(|e| e.to_string())?;
    std::io::Write::write_all(&mut file, frame.data(0)).map_err(|e| e.to_string())?;

    Ok(())
}
