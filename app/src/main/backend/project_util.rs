use serde::{Serialize, Deserialize};
use std::path::{Path}
use std::fs;

#[derive(Serialize, Deserialize, Debug)]
pub struct Project {
    pub name: String,
}

pub fn loadProjects(path: &str) -> Result<Vec<Project>, String> {
    let file = Path::new(path);

    if !file.exists() {
        return Ok(vec![]);
    }

    let mut data = std::fs::read(&file)
        .map_err(|e| format!("Failed to read: {}", e))?;

    let projects: Vec<Project> =
        simd_json::serde::from_slice(&mut data).unwrap_or_else(|_| vec![]);

    Ok(projects)
}

pub fn saveProjects(path: &str, projects: &Vec<Project>) -> Result<(), String> {
    let file = Path::new(path);

    let json = serde_json::to_string(projects)
        .map_err(|e| format!("Failed serialize: {}", e))?;

    std::fs::write(&file, json).map_err(|e| format!("Failed to save: {}", e))
}

