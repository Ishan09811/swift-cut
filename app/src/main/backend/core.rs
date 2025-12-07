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

    let codec_ctx = CodecContext::from_parameters(input_stream.parameters()).map_err(|e| e.to_string())?;
    let mut decoder = codec_ctx.decoder().video().unwrap();

    let mut scaler = Scaler::get(
        decoder.format(),
        decoder.width(),
        decoder.height(),
        ffmpeg::format::Pixel::RGB24,
        200,
        112,
        Flags::FAST_BILINEAR,
    ).unwrap();

    let mut keyframe_count = 0;

    for (stream, packet) in ictx.packets() {
        if stream.index() != idx {
            continue;
        }

        let is_key = packet.is_key();
        
        decoder.send_packet(&packet).unwrap();

        loop {
            let mut frame = Video::empty();
            if decoder.receive_frame(&mut frame).is_err() {
                break;
            }
            if is_key {
                let mut rgb = Video::empty();
                scaler.run(&frame, &mut rgb).unwrap();

                let out = format!("{}/thumb_{}.ppm", out_dir, keyframe_count);
                save_ppm(&rgb, &out)?;
                keyframe_count += 1;
            }
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
