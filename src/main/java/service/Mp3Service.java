package main.java.service;

import com.mpatric.mp3agic.*;
import main.java.exceptions.FileNameBadFormatException;
import main.java.repositories.Mp3Repository;
import main.java.util.FileHelper;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Service
public class Mp3Service {

    private static Logger logger = Logger.getLogger(Mp3Service.class);

    @Autowired
    Mp3Repository mp3Repository;

}
