import unittest
from unittest.mock import patch, MagicMock
import sys
import os
import shutil
from main import architecture_info, autorun, pernyataan, handle_exit, main

class TestMain(unittest.TestCase):

    @patch('main.sys.version', '3.8.5 (tags/v3.8.5:580fbb0, Jul 20 2020, 12:11:08) [MSC v.1916 64 bit (AMD64)]')
    def test_get_architecture_info(self):
        self.assertEqual(architecture_info.get_architecture_info(), 'MSC v.1916 64 bit (AMD64)')

    @patch('main.win32api.GetVersionEx')
    def test_get_windows_version(self, mock_get_version_ex):
        mock_get_version_ex.return_value = (10, 0, 0, 0, 0)
        self.assertEqual(architecture_info.get_windows_version(), 'Windows 10')

        mock_get_version_ex.return_value = (6, 3, 0, 0, 0)
        self.assertEqual(architecture_info.get_windows_version(), 'Windows 8.1')

        mock_get_version_ex.return_value = (5, 1, 0, 0, 0)
        self.assertEqual(architecture_info.get_windows_version(), 'Windows XP')

    @patch('main.os.path.exists')
    @patch('main.win32api.GetVolumeInformation')
    @patch('main.shutil.copyfile')
    def test_autorun(self, mock_copyfile, mock_get_volume_info, mock_path_exists):
        mock_path_exists.side_effect = lambda x: True
        mock_get_volume_info.return_value = ('USB_DRIVE', '', '', '', 'FAT32')

        # Dynamically use the current active drive (using os.getcwd())
        current_drive = os.path.splitdrive(os.getcwd())[0] + "\\"
        autorun(current_drive, 'autorun.ico', 'app.exe')

        mock_copyfile.assert_any_call(os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "Autorun.ico")), current_drive + 'autorun.ico')
        mock_copyfile.assert_any_call(os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "output", "app.exe")), current_drive + 'app.exe')

    @patch('builtins.input', side_effect=['', 'autorun.ico', 'app.exe'])
    @patch('main.os.path.exists', return_value=True)
    def test_pernyataan(self, mock_path_exists, mock_input):
        # Dynamically use the current active drive (using os.getcwd())
        current_drive = os.path.splitdrive(os.getcwd())[0] + "\\"
        drive_letter, name, apps = pernyataan()
        self.assertEqual(drive_letter, current_drive)
        self.assertEqual(name, 'autorun.ico')
        self.assertEqual(apps, 'app.exe')

    @patch('builtins.input', side_effect=['y'])
    @patch('main.sys.exit')
    def test_handle_exit(self, mock_exit, mock_input):
        handle_exit()
        mock_exit.assert_called_once_with(0)

    @patch('builtins.input', side_effect=[os.path.join('', ''), 'autorun.ico', 'app.exe', 'y'])
    @patch('main.pernyataan', return_value=(os.path.join('', ''), 'autorun.ico', 'app.exe'))
    @patch('main.autorun')
    @patch('main.architecture_info.get_architecture_info', return_value='x64')
    @patch('main.architecture_info.get_windows_version', return_value='Windows 10')
    def test_main(self, mock_get_windows_version, mock_get_architecture_info, mock_autorun, mock_pernyataan, mock_input):
        with patch('main.print'):
            main()
        mock_autorun.assert_called_once_with(os.path.join('', ''), 'autorun.ico', 'app.exe')

if __name__ == '__main__':
    unittest.main()
