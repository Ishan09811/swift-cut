
mod project_util

use ffmpeg_next as ffmpeg;
use ffmpeg::{
    codec::Context as CodecContext,
    media::Type,
};
use ffmpeg::format::input;
use ffmpeg::software::scaling::{context::Context as Scaler, flag::Flags};
use ffmpeg::util::frame::video::Video;
use std::sync::OnceLock;

static ROOT_PATH: OnceLock<String> = OnceLock::new();

pub fn init(path: &str) -> Result<(), String> {
    ROOT_PATH
        .set(path.to_string())
        .map_err(|_| "Root path already initialized".to_string())
}

pub fn getRootPath() -> Result<&'static str, String> {
    ROOT_PATH
        .get()
        .map(|s| s.as_str())
        .ok_or_else(|| "Root path not initialized".to_string())
}

pub fn saveProjects(projects: &Vec<Project>) -> Result<(), String> { 
    project_util::saveProjects(projects);
}

pub fn loadProjects() -> Result<Vec<Project>, String> {
    let root = getRootPath()?; 
    let filePath = format!("{}/{}", root, "projects.json");
    project_util::loadProjects(filePath);
}

pub fn extractThumbnails(video: &str, out_dir: &str) -> Result<(), String> {
    ffmpeg::init().map_err(|e| e.to_string())?;

    let mut ictx = input(&video).map_err(|e| e.to_string())?;
    let inputStream = ictx.streams().best(Type::Video).ok_or("No video stream")?;
    let idx = inputStream.index();

    let codecCtx = CodecContext::from_parameters(inputStream.parameters()).map_err(|e| e.to_string())?;
    let mut decoder = codecCtx.decoder().video().unwrap();

    let mut scaler = Scaler::get(
        decoder.format(),
        decoder.width(),
        decoder.height(),
        ffmpeg::format::Pixel::RGB24,
        160,
        90,
        Flags::FAST_BILINEAR,
    ).unwrap();

    let mut keyframeCount = 0;

    for (stream, packet) in ictx.packets() {
        if stream.index() != idx {
            continue;
        }

        if !packet.is_key() {
            continue;
        }
        
        decoder.send_packet(&packet).unwrap();

        let mut frame = Video::empty();
        if decoder.receive_frame(&mut frame).is_ok() {
            let mut rgb = Video::empty();
            scaler.run(&frame, &mut rgb).unwrap();

            let out = format!("{}/thumb_{}.ppm", out_dir, keyframe_count);
            savePPM(&rgb, &out)?;
            keyframeCount += 1;
        }
    }

    Ok(())
}

fn savePPM(frame: &Video, path: &str) -> Result<(), String> {
    let mut file = std::fs::File::create(path).map_err(|e| e.to_string())?;

    let header = format!("P6\n{} {}\n255\n", frame.width(), frame.height());
    std::io::Write::write_all(&mut file, header.as_bytes()).map_err(|e| e.to_string())?;
    std::io::Write::write_all(&mut file, frame.data(0)).map_err(|e| e.to_string())?;

    Ok(())
}
