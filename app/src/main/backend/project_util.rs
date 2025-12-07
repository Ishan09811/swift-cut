use serde::{Serialize, Deserialize};
use std::path::Path;
use std::fs;

#[derive(Serialize, Deserialize, Debug)]
pub struct Project {
    pub name: String,
}

pub fn loadProjects(path: &str) -> Result<String, String> {
    let file = Path::new(path);

    if !file.exists() {
        return Ok("");
    }

    let mut data = std::fs::read(&file)
        .map_err(|e| format!("Failed to read: {}", e))?;

    let projects: Vec<Project> =
        simd_json::serde::from_slice(&mut data).unwrap_or_else(|_| "");
    
    let json = serde_json::to_string(&projects).map_err(|e| e.to_string())?;

    Ok(json)
}

pub fn saveProjects(path: &str, json: &str) -> Result<(), String> {
    let file = Path::new(path);
    std::fs::write(&file, json).map_err(|e| format!("Failed to save: {}", e))
}

