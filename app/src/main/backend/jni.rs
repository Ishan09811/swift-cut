use jni::objects::{JClass, JString};
use jni::sys::{jint};
use jni::JNIEnv;
use std::ffi::CString;
use std::path::Path;
use std::fs;

#[no_mangle]
pub extern "C" fn Java_io_github_swiftcut_NativeLib_extractThumbnails(
    env: JNIEnv,
    _class: JClass,
    j_video_path: JString,
    j_out_dir: JString,
    j_count: jint,
) -> jint {
    let video_path: String = match env.get_string(j_video_path) {
        Ok(s) => s.into(),
        Err(_) => return -1,
    };
    let out_dir: String = match env.get_string(j_out_dir) {
        Ok(s) => s.into(),
        Err(_) => return -2,
    };
    let count: i32 = j_count as i32;

    if let Err(_) = fs::create_dir_all(&out_dir) {
        return -3;
    }

    // TODO: implement timeline extractor

    0
}
