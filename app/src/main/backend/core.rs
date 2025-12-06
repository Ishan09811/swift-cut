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

    let input_stream = ictx
        .streams()
        .best(Type::Video)
        .ok_or("No video stream")?;

    let duration_us = input_stream.duration();
    let duration_ms = duration_us as i64 / 1000;

    let count = (duration_ms / 500).max(1) as i32;

    let params = input_stream.parameters();
    let codec_ctx = CodecContext::from_parameters(params)
        .map_err(|e| e.to_string())?;

    let mut decoder = codec_ctx.decoder().video().map_err(|e| e.to_string())?;

    let mut scaler = Scaler::get(
        decoder.format(),
        decoder.width(),
        decoder.height(),
        ffmpeg::format::Pixel::RGB24,
        160,
        90,
        Flags::FAST_BILINEAR,
    )
    .unwrap();

    std::fs::create_dir_all(out_dir).map_err(|e| e.to_string())?;

    for i in 0..count {
        let ts = (duration_us as f64 * (i as f64 / count as f64)) as i64;
        ictx.seek(ts, ..).map_err(|e| e.to_string())?;
        let mut decoded = false;
        
        for (stream, packet) in ictx.packets() {
            if stream.index() != input_stream.index() {
                continue;
            }

            decoder.send_packet(&packet).unwrap();

            let mut frame = Video::empty();
            if decoder.receive_frame(&mut frame).is_ok() {
                let mut rgb_frame = Video::empty();
                scaler.run(&frame, &mut rgb_frame).unwrap();

                let out = format!("{}/thumb_{}.ppm", out_dir, i);
                save_ppm(&rgb_frame, &out)?;

                decoded = true;
                break;
            }
        }

        if !decoded {
            return Err("Failed to decode frame".into());
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
