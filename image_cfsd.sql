-- phpMyAdmin SQL Dump
-- version 3.4.5
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Nov 22, 2012 at 10:18 PM
-- Server version: 5.5.16
-- PHP Version: 5.3.8

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `image_cfsd`
--

-- --------------------------------------------------------

--
-- Table structure for table `imagedata`
--

CREATE TABLE IF NOT EXISTS `imagedata` (
  `Image_ID` int(11) NOT NULL AUTO_INCREMENT,
  `Image_Path` varchar(1000) NOT NULL,
  `redSFD` double NOT NULL,
  `greenSFD` double NOT NULL,
  `blueSFD` double NOT NULL,
  PRIMARY KEY (`Image_ID`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=127 ;

--
-- Dumping data for table `imagedata`
--

-- --------------------------------------------------------

--
-- Table structure for table `quantizedvalues`
--

CREATE TABLE IF NOT EXISTS `quantizedvalues` (
  `Image_ID` int(11) NOT NULL,
  `qRed` int(11) NOT NULL,
  `qGreen` int(11) NOT NULL,
  `qBlue` int(11) NOT NULL,
  PRIMARY KEY (`Image_ID`,`qRed`,`qGreen`,`qBlue`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `quantizedvalues`
--

--
-- Constraints for dumped tables
--

--
-- Constraints for table `quantizedvalues`
--
ALTER TABLE `quantizedvalues`
  ADD CONSTRAINT `quantizedvalues_ibfk_1` FOREIGN KEY (`Image_ID`) REFERENCES `imagedata` (`Image_ID`);

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
