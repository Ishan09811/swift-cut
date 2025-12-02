use ffmpeg_next as ffmpeg;
use ffmpeg::format::input;
use ffmpeg::software::scaling::{context::Context as Scaler, flag::Flags};
use ffmpeg::util::frame::video::Video;
use std::path::Path;

pub fn extract_thumbnails(video: &str, out_dir: &str, count: i32) -> Result<(), String> {
    ffmpeg::init().map_err(|e| e.to_string())?;

    let mut ictx = input(&video).map_err(|e| e.to_string())?;

    let input_stream = ictx
        .streams()
        .best(ffmpeg::media::Type::Video)
        .ok_or("No video stream")?;

    let video_stream_index = input_stream.index();

    let mut decoder = input_stream.codec().decoder().video().map_err(|e| e.to_string())?;

    let total_frames = input_stream.frames();
    let step = (total_frames / count as i64).max(1);

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

    let mut frame_index = 0;
    let mut extracted = 0;

    for (stream, packet) in ictx.packets() {
        if stream.index() != video_stream_index {
            continue;
        }

        decoder.send_packet(&packet).unwrap();

        let mut frame = Video::empty();
        while decoder.receive_frame(&mut frame).is_ok() {
            if frame_index % step == 0 {
                let mut rgb_frame = Video::empty();
                scaler.run(&frame, &mut rgb_frame).unwrap();

                let out = format!("{}/thumb_{}.ppm", out_dir, extracted);
                save_ppm(&rgb_frame, &out)?;

                extracted += 1;
                if extracted >= count {
                    return Ok(());
                }
            }

            frame_index += 1;
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
