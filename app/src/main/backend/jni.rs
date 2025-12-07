
mod core;

use jni::objects::{JClass, JString};
use jni::sys::{jint};
use jni::JNIEnv;
use std::fs;

#[unsafe(no_mangle)]
pub extern "C" fn Java_io_github_swiftcut_NativeLib_init(
    mut env: JNIEnv,
    _class: JClass,
    jRootPath: JString
) -> jint {
    let rootPath: String = match env.get_string(&jRootPath) {
        Ok(s) => s.into(),
        Err(_) => return -2,
    };

    match core::init(&rootPath) {
        Ok(_) => 0,
        Err(_) => -4,
    }
}

#[unsafe(no_mangle)]
pub extern "C" fn Java_io_github_swiftcut_NativeLib_extractThumbnails(
    mut env: JNIEnv,
    _class: JClass,
    jVideoPath: JString,
    jOutDir: JString,
) -> jint {
    let videoPath: String = match env.get_string(&jVideoPath) {
        Ok(s) => s.into(),
        Err(_) => return -1,
    };
    let outDir: String = match env.get_string(&jOutDir) {
        Ok(s) => s.into(),
        Err(_) => return -2,
    };

    if let Err(_) = fs::create_dir_all(&outDir) {
        return -3;
    }

    match core::extract_thumbnails(&videoPath, &outDir) {
        Ok(_) => 0,
        Err(_) => -4,
    }
}
